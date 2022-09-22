package com.qlive.uikitlinkmic

import android.graphics.Color
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveUser
import com.qlive.roomservice.QRoomService
import com.qlive.sdk.QLive
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.ext.ViewUtil
import com.qlive.uikitcore.ext.bg
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import com.qlive.uikitcore.view.SimpleDividerDecoration
import kotlinx.android.synthetic.main.kit_item_linkable.view.*
import kotlinx.android.synthetic.main.kit_online_linkable_dialog.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OnlineLinkableUserDialog(private val roomService: QRoomService) : FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    private val mAdapter = OnlineUserAdapter()
    override fun getViewLayoutId(): Int {
        return R.layout.kit_online_linkable_dialog
    }

    fun setInviteCall(inviteCall: (room: QLiveUser) -> Unit) {
        mAdapter.inviteCall = inviteCall
    }

    private suspend fun suspendLoad(page: Int) = suspendCoroutine<List<QLiveUser>> { ct ->
        roomService.getOnlineUser(page + 1, 20, object : QLiveCallBack<List<QLiveUser>> {
            override fun onError(p0: Int, msg: String?) {
                ct.resumeWithException(Exception(msg))
            }

            override fun onSuccess(data: List<QLiveUser>?) {
                ct.resume(data ?: ArrayList<QLiveUser>())
            }
        })
    }

    private fun load(page: Int) {
        bg {
            //后台运行
            doWork {
                val data = suspendLoad(page).filter {
                    it.userId != QLive.getLoginUser().userId
                }
                mSmartRecyclerView.onFetchDataFinish(data, false)
            }
            //运行出错
            catchError {
                mSmartRecyclerView.onFetchDataError()
            }
            //最后收尾
            onFinally {
            }
        }
    }

    override fun init() {
        mSmartRecyclerView.recyclerView.addItemDecoration(
            SimpleDividerDecoration(
                requireContext(),
                Color.parseColor("#EAEAEA"), ViewUtil.dip2px(1f)
            )
        )
        mSmartRecyclerView.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mSmartRecyclerView.setUp(mAdapter, true, true) {
            load(it)
        }
        mSmartRecyclerView.startRefresh()
    }

    class OnlineUserAdapter : QSmartAdapter<QLiveUser>(
        R.layout.kit_item_linkable
    ) {
        var inviteCall: (user: QLiveUser) -> Unit = {
        }

        override fun convert(helper: QRecyclerViewHolder, item: QLiveUser) {
            Glide.with(mContext).load(item.avatar)
                .into(helper.itemView.ivAvatar)
            helper.itemView.tvUserName.text = item.nick
            helper.itemView.ivInvite.setOnClickListener {
                inviteCall.invoke(item)
            }
        }
    }
}