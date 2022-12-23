package com.qlive.uikitlinkmic

import android.graphics.Color
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveUser
import com.qlive.roomservice.QRoomService
import com.qlive.sdk.QLive
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.backGround
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.ViewUtil
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikitcore.view.SimpleDividerDecoration
import com.qlive.uikitlinkmic.databinding.KitItemLinkableBinding
import com.qlive.uikitlinkmic.databinding.KitOnlineLinkableDialogBinding
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OnlineLinkableUserDialog(private val roomService: QRoomService) :
    ViewBindingDialogFragment<KitOnlineLinkableDialogBinding>() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    private val mAdapter = OnlineUserAdapter()

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
        backGround {
            //后台运行
            doWork {
                val data = suspendLoad(page).filter {
                    it.userId != QLive.getLoginUser().userId
                }
                binding.mSmartRecyclerView.onFetchDataFinish(data, false)
            }
            //运行出错
            catchError {
                binding.mSmartRecyclerView.onFetchDataError()
            }
            //最后收尾
            onFinally {
            }
        }
    }

    override fun init() {
        binding.mSmartRecyclerView.recyclerView.addItemDecoration(
            SimpleDividerDecoration(
                requireContext(),
                Color.parseColor("#EAEAEA"), ViewUtil.dip2px(1f)
            )
        )
        binding.mSmartRecyclerView.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.mSmartRecyclerView.setUp(mAdapter, true, true) {
            load(it)
        }
        binding.mSmartRecyclerView.startRefresh()
    }

    class OnlineUserAdapter : QSmartViewBindAdapter<QLiveUser, KitItemLinkableBinding>() {
        var inviteCall: (user: QLiveUser) -> Unit = {
        }

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<KitItemLinkableBinding>,
            item: QLiveUser
        ) {
            Glide.with(mContext).load(item.avatar)
                .into(helper.binding.ivAvatar)
            helper.binding.tvUserName.text = item.nick
            helper.binding.ivInvite.setOnClickListener {
                inviteCall.invoke(item)
            }
        }
    }
}