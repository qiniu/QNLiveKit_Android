package com.qlive.uiwidghtbeauty.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.qlive.uiwidghtbeauty.R
import com.qlive.uiwidghtbeauty.adapter.NativeStickerAdapter
import com.qlive.uiwidghtbeauty.adapter.StickerAdapter
import com.qlive.uiwidghtbeauty.model.EffectState
import com.qlive.uiwidghtbeauty.model.StickerItem
import com.qlive.uiwidghtbeauty.utils.FileUtils
import com.qlive.uiwidghtbeauty.utils.ToastUtils
import com.qlive.uiwidghtbeauty.utils.Utils
import com.softsugar.library.api.Material.downLoadZip
import com.softsugar.library.api.Material.getDataListSync
import com.softsugar.library.sdk.entity.MaterialEntity
import com.softsugar.library.sdk.listener.DownloadListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("ViewConstructor")
class StickerPageItemView(
    context: Context,
    val assetsIndex: String,
    private val groupIndex: String
) :
    RecyclerView(context) {
    private val mStickerItem = ArrayList<StickerItem>()
    var onStickerItemClick: (assetsIndex: String, groupIndex: String, item: StickerItem, itemIndex: Int) -> Unit =
        { _: String, _: String, _: StickerItem, _: Int ->
        }

    private val mNativeStickerAdapter by lazy {
        NativeStickerAdapter(mStickerItem, context).apply {
            setClickStickerListener {
                val position: Int = it.tag.toString().toInt()
                mStickerItem[position]
                onStickerItemClick.invoke(
                    assetsIndex,
                    groupIndex,
                    mStickerItem[position],
                    position
                )
            }
        }
    }

    private val mStickerAdapter by lazy {
        StickerAdapter(mStickerItem, context).apply {
            setClickStickerListener { v ->
                if (!Utils.isNetworkAvailable(getContext())) {
                    runOnUiThread(Runnable {
                        Toast.makeText(
                            getContext(),
                            "Network unavailable.",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                }

                val position = v.tag.toString().toInt()
                val stickerItem: StickerItem = getItem(position)?:return@setClickStickerListener
                if (stickerItem.state === EffectState.LOADING_STATE) {
                    return@setClickStickerListener
                }
                val (_, _, _, _, pkgUrl) = stickerItem.material
                preMaterialUrl = pkgUrl
                //如果素材还未下载，点击时需要下载
                //如果素材还未下载，点击时需要下载
                if (stickerItem.state === EffectState.NORMAL_STATE) {
                    stickerItem.state = EffectState.LOADING_STATE
                    notifyStickerViewState(stickerItem, position, groupIndex)
                    downLoadZip(pkgUrl, object : DownloadListener {
                        override fun onStart() {}
                        override fun onFail(errorInfo: String) {
                            ToastUtils.showShortToast(
                                context,
                                String.format(Locale.getDefault(), "素材下载失败:%s", errorInfo)
                            )
                            runOnUiThread(Runnable {
                                stickerItem.state = EffectState.NORMAL_STATE
                                notifyStickerViewState(stickerItem, position, groupIndex)
                            })
                        }

                        override fun onFinish(path: String) {
                            runOnUiThread(Runnable {
                                stickerItem.path = path
                                stickerItem.state = EffectState.DONE_STATE
                                //如果本次下载是用户用户最后一次选中项，则直接应用
                                if (preMaterialUrl == pkgUrl) {
                                    onStickerItemClick.invoke(
                                        assetsIndex,
                                        groupIndex,
                                        mStickerItem[position],
                                        position
                                    )
                                }
                                notifyStickerViewState(stickerItem, position, groupIndex)
                            })
                        }

                        override fun onProgress(i: Int) {}
                    })
                } else if (stickerItem.state === EffectState.DONE_STATE) {
                    onStickerItemClick.invoke(
                        assetsIndex,
                        groupIndex,
                        mStickerItem[position],
                        position
                    )
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelect(index: Int, groupIndex: String) {
        if (isLoadFromLocal) {
            if (groupIndex == this.groupIndex) {
                mNativeStickerAdapter.setSelectedPosition(index)
            } else {
                mNativeStickerAdapter.setSelectedPosition(-1)
            }
            mNativeStickerAdapter.notifyDataSetChanged()
        } else {
            if (groupIndex == this.groupIndex) {
                mStickerAdapter.setSelectedPosition(index)
            } else {
                mStickerAdapter.setSelectedPosition(-1)
            }
            mStickerAdapter.notifyDataSetChanged()
        }
    }

    init {
        layoutManager = GridLayoutManager(context, 6)
        addItemDecoration(SpaceItemDecoration(0));
    }

    private var isLoadFromLocal = true
    fun attach(loadFromLocal: Boolean) {
        isLoadFromLocal = loadFromLocal
        GlobalScope.launch(Dispatchers.Main) {
            mStickerItem.clear()
            val ret = async(Dispatchers.IO) {
                try {
                    if (loadFromLocal) {
                        mStickerItem.addAll(FileUtils.getStickerFiles(context, assetsIndex))
                    } else {
                        val materialEntities = getDataListSync(groupIndex)
                        mStickerItem.addAll(
                            fetchStickerGroupMaterialInfo(
                                groupIndex,
                                materialEntities
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ret.await()
            if (loadFromLocal) {
                adapter = mNativeStickerAdapter
            } else {
                adapter = mStickerAdapter
            }
        }
    }

    /**
     * 初始化素材的基本信息，如缩略图，是否已经缓存
     *
     * @param groupId   组id
     * @param materials 服务器返回的素材list
     */
    private fun fetchStickerGroupMaterialInfo(
        groupId: String,
        materials: List<MaterialEntity>?,
    ): ArrayList<StickerItem> {
        if (materials == null || materials.size <= 0) {
            return ArrayList<StickerItem>()
        }
        val stickerList = ArrayList<StickerItem>()
        for (i in materials.indices) {
            val (_, name, _, thumbnail, _, zipSdPath) = materials[i]
            stickerList.add(StickerItem(name, Uri.parse(thumbnail), zipSdPath).apply {
                material = materials.get(i)
            })
        }
        return stickerList
    }

    /**
     * 直接变更 ui ,不通过数据驱动，相比 notifyDataSetChanged 反应会快些
     *
     * @param stickerItem
     * @param position
     * @param name
     */
    fun notifyStickerViewState(stickerItem: StickerItem, position: Int, name: String?) {
        val viewHolder: ViewHolder = findViewHolderForAdapterPosition(position) ?: return

        val itemView = viewHolder.itemView
        val normalState = itemView.findViewById<ImageView>(R.id.normalState)
        val downloadingState = itemView.findViewById<ImageView>(R.id.downloadingState)
        val loadingStateParent = itemView.findViewById<ViewGroup>(R.id.loadingStateParent)
        when (stickerItem.state) {
            EffectState.NORMAL_STATE ->                 //设置为等待下载状态
                if (normalState.visibility != VISIBLE) {
                    normalState.visibility = VISIBLE
                    downloadingState.visibility = INVISIBLE
                    downloadingState.isActivated = false
                    loadingStateParent.visibility = INVISIBLE
                }
            EffectState.LOADING_STATE ->                 //设置为loading 状态
                if (downloadingState.visibility != VISIBLE) {
                    normalState.visibility = INVISIBLE
                    downloadingState.isActivated = true
                    downloadingState.visibility = VISIBLE
                    loadingStateParent.visibility = VISIBLE
                }
            EffectState.DONE_STATE ->                 //设置为下载完成状态
                if (normalState.visibility != INVISIBLE || downloadingState.visibility != INVISIBLE) {
                    normalState.visibility = INVISIBLE
                    downloadingState.visibility = INVISIBLE
                    downloadingState.isActivated = false
                    loadingStateParent.visibility = INVISIBLE
                }
            else -> {}
        }
    }
}




