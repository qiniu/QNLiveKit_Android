package com.qlive.uiwidghtbeauty.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.qlive.uiwidghtbeauty.QSenseTimeManager.sSenseTimePlugin
import com.qlive.uiwidghtbeauty.R
import com.qlive.uiwidghtbeauty.model.EffectState
import com.qlive.uiwidghtbeauty.model.MakeupItem
import com.qlive.uiwidghtbeauty.utils.Constants.ST_MAKEUP_STYLE
import com.qlive.uiwidghtbeauty.utils.ResourcesUtil
import com.qlive.uiwidghtbeauty.utils.ToastUtils
import com.qlive.uiwidghtbeauty.utils.Utils
import com.sensetime.stmobile.params.STEffectBeautyGroup
import com.softsugar.library.api.Material
import com.softsugar.library.api.Material.downLoadZip
import com.softsugar.library.sdk.entity.MaterialEntity
import com.softsugar.library.sdk.listener.DownloadListener
import java.util.*

fun MakeUpBeautyPage.fetchMakeupGroupMaterialList(makeupGroupIds: java.util.HashMap<String, String>) {
    makeupGroupIds.forEach {
        // 从服务器拉取素材
        val materialEntities: List<MaterialEntity> = Material.getDataListSync(it.value)
        fetchMakeupGroupMaterialInfo(it.key, materialEntities)
    }
}

private fun MakeUpBeautyPage.fetchMakeupGroupMaterialInfo(
    groupType: String,
    materials: List<MaterialEntity>
) {
    materials.forEach { entity ->
        var bitmap: Bitmap? = null
        try {
            bitmap = Utils.getImageSync(entity.thumbnail, context)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.none)
        }
        mMakeupLists[groupType]!!.add(MakeupItem(entity.name, bitmap, entity.zipSdPath))
    }
    initMakeupListener(groupType, materials)
    post(Runnable { mMakeupAdapters[groupType]!!.notifyDataSetChanged() })
}

// 记录用户最后一次点击的素材 url ,包括还未下载的，方便下载完成后，直接应用素材
var preMaterialUrl = ""

@SuppressLint("NotifyDataSetChanged")
private fun MakeUpBeautyPage.initMakeupListener(
    groupType: String,
    materials: List<MaterialEntity>
) {
    mMakeupAdapters[groupType]!!.setClickMakeupListener(View.OnClickListener { v ->
        val position = v.tag.toString().toInt()
        mMakeupAdapters[groupType]!!.setSelectedPosition(position)
        if (position == 0) {
            mMakeupOptionSelectedIndex[mMakeupOptionIndex[groupType]!!] = position
            mFilterStrengthLayout.visibility = View.INVISIBLE
            if (mCurrentMakeupGroupIndex == ST_MAKEUP_STYLE) {
                mCurrentStylePath = ""
               sSenseTimePlugin!!.setBeautyGroupStrength(
                    STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP,
                    mCurrentStylePath,
                    mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                )
               sSenseTimePlugin!!.setBeautyGroupStrength(
                    STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER,
                    mCurrentStylePath,
                    mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                )
            } else {
               sSenseTimePlugin!!.setMakeup(mCurrentMakeupGroupIndex, "")
            }
            updateMakeupOptions(mCurrentMakeupGroupIndex, false)
        } else {
            if (!Utils.isNetworkAvailable(context.applicationContext)) {
                post (Runnable {
                    Toast.makeText(
                        context.applicationContext,
                        "Network unavailable.",
                        Toast.LENGTH_LONG
                    ).show()
                })
            }
            val makeupItem = mMakeupAdapters[groupType]!!.getItem(position)
            if (makeupItem != null && makeupItem.state === EffectState.LOADING_STATE) {
                ToastUtils.showShortToast(
                    context.applicationContext,
                    String.format(Locale.getDefault(), "正在下载，请稍后点击!")
                )
                return@OnClickListener
            }
            // 减 2 是因为每一种类型的美妆都包含了一种原始的无特效和本地的一种特效
            if (position - 2 < 0) {
                // 本地素材
                mMakeupOptionSelectedIndex[mMakeupOptionIndex[groupType]!!] = position
                if (mCurrentMakeupGroupIndex == ST_MAKEUP_STYLE) {
                    mCurrentStylePath =
                        mMakeupLists[ResourcesUtil.getMakeupNameOfType(mCurrentMakeupGroupIndex)]!![position].path
                   sSenseTimePlugin!!.setBeautyGroupStrength(
                        STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP,
                        mCurrentStylePath,
                        mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                    )
                   sSenseTimePlugin!!.setBeautyGroupStrength(
                        STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER,
                        mCurrentStylePath,
                        mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                    )
                } else {
                   sSenseTimePlugin!!.setMakeup(
                        mCurrentMakeupGroupIndex,
                        mMakeupLists[ResourcesUtil.getMakeupNameOfType(mCurrentMakeupGroupIndex)]!![position].path
                    )
                   sSenseTimePlugin!!.setMakeupStrength(
                        mCurrentMakeupGroupIndex,
                        mMakeupStrength[mCurrentMakeupGroupIndex]!!.toFloat() / 100f
                    )
                }
                mFilterStrengthLayout.visibility = View.VISIBLE
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                updateMakeupOptions(mCurrentMakeupGroupIndex, true)
            } else {
                // 在线拉取远程素材
                val (_, _, _, _, pkgUrl) = materials[position - 2]
                preMaterialUrl = pkgUrl
                //如果素材还未下载，点击时需要下载
                if (makeupItem!!.state === EffectState.NORMAL_STATE) {
                    makeupItem!!.state = EffectState.LOADING_STATE
                    notifyMakeupViewState(makeupItem, position, groupType)
                    downLoadZip(pkgUrl, object : DownloadListener {
                        override fun onStart() {}
                        override fun onFail(errorInfo: String) {
                            post(Runnable {
                                ToastUtils.showShortToast(
                                    context, String.format(
                                        Locale.getDefault(), "素材下载失败:%s", errorInfo
                                    )
                                )
                                makeupItem.state = EffectState.NORMAL_STATE
                                notifyMakeupViewState(makeupItem, position, groupType)
                            })
                        }

                        override fun onFinish(path: String) {
                            makeupItem.path = path
                            makeupItem.state = EffectState.DONE_STATE
                            if (preMaterialUrl == pkgUrl) {
                                if (mCurrentMakeupGroupIndex == ST_MAKEUP_STYLE) {
                                    mCurrentStylePath = makeupItem.path
                                    post(Runnable {
                                       sSenseTimePlugin!!.setBeautyGroupStrength(
                                            STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP,
                                            mCurrentStylePath,
                                            mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                                        )
                                       sSenseTimePlugin!!.setBeautyGroupStrength(
                                            STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER,
                                            mCurrentStylePath,
                                            mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                                        )
                                    })
                                } else {
                                    post(Runnable {
                                       sSenseTimePlugin!!.setMakeup(
                                            mCurrentMakeupGroupIndex,
                                            makeupItem.path
                                        )
                                    })
                                }
                            }
                            post(Runnable {
                                notifyMakeupViewState(makeupItem, position, groupType)
                            })
                        }

                        override fun onProgress(i: Int) {}
                    })
                } else if (makeupItem!!.state === EffectState.DONE_STATE) {
                    if (mCurrentMakeupGroupIndex == ST_MAKEUP_STYLE) {
                        mCurrentStylePath = mMakeupAdapters[groupType]!!.getItem(position).path
                       sSenseTimePlugin!!.setBeautyGroupStrength(
                            STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP,
                            mCurrentStylePath,
                            mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                        )
                       sSenseTimePlugin!!.setBeautyGroupStrength(
                            STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER,
                            mCurrentStylePath,
                            mMakeupStrength[mCurrentMakeupGroupIndex]!! / 100.0f
                        )
                    } else {
                       sSenseTimePlugin!!.setMakeup(
                            mCurrentMakeupGroupIndex, mMakeupAdapters[groupType]!!
                                .getItem(position).path
                        )
                    }
                }
                mFilterStrengthLayout.visibility = View.VISIBLE
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionSelectedIndex[mMakeupOptionIndex[groupType]!!] = position
                updateMakeupOptions(mCurrentMakeupGroupIndex, true)
            }
        }
        mMakeupAdapters[groupType]!!.notifyDataSetChanged()
    })

}

/**
 * 直接变更 ui ,不通过数据驱动，相比 notifyDataSetChanged 反应会快些
 *
 * @param makeupItem
 * @param position
 * @param name
 */
fun MakeUpBeautyPage.notifyMakeupViewState(makeupItem: MakeupItem, position: Int, name: String?) {
    val viewHolder = mMakeupOptionsRecycleView.findViewHolderForAdapterPosition(position)
    //排除不必要变更
    if (viewHolder == null || mMakeupOptionsRecycleView.adapter !== mMakeupAdapters[name]) {
        return
    }
    val itemView = viewHolder.itemView
    val normalState = itemView.findViewById<ImageView>(R.id.normalState)
    val downloadingState = itemView.findViewById<ImageView>(R.id.downloadingState)
    val loadingStateParent = itemView.findViewById<ViewGroup>(R.id.loadingStateParent)
    when (makeupItem.state) {
        EffectState.NORMAL_STATE ->                 //设置为等待下载状态
            if (normalState.visibility != FrameLayout.VISIBLE) {
                normalState.visibility = FrameLayout.VISIBLE
                downloadingState.visibility = FrameLayout.INVISIBLE
                downloadingState.isActivated = false
                loadingStateParent.visibility = FrameLayout.INVISIBLE
            }
        EffectState.LOADING_STATE ->                 //设置为loading 状态
            if (downloadingState.visibility != FrameLayout.VISIBLE) {
                normalState.visibility = FrameLayout.INVISIBLE
                downloadingState.isActivated = true
                downloadingState.visibility = FrameLayout.VISIBLE
                loadingStateParent.visibility = FrameLayout.VISIBLE
            }
        EffectState.DONE_STATE ->                 //设置为下载完成状态
            if (normalState.visibility != FrameLayout.INVISIBLE || downloadingState.visibility != FrameLayout.INVISIBLE) {
                normalState.visibility = FrameLayout.INVISIBLE
                downloadingState.visibility = FrameLayout.INVISIBLE
                downloadingState.isActivated = false
                loadingStateParent.visibility = FrameLayout.INVISIBLE
            }
        else -> {}
    }
}