package com.qlive.uikitpk

import android.graphics.Color
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.sdk.QLive
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.ext.ViewUtil
import com.qlive.uikitcore.ext.bg
import com.qlive.uikitcore.view.SimpleDividerDecoration
import kotlinx.android.synthetic.main.kit_dialog_pklist.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * pk列表弹窗
 */
class PKAbleListDialog() : FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    private val mAdapter = PKAnchorListAdapter()

    fun setInviteCall(inviteCall: (room: QLiveRoomInfo) -> Unit) {
        mAdapter.inviteCall = inviteCall
    }

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_pklist
    }

    private suspend fun suspendLoad(page: Int) = suspendCoroutine<List<QLiveRoomInfo>> { ct ->
        QLive.getRooms().listRoom(page + 1, 20, object : QLiveCallBack<List<QLiveRoomInfo>> {
            override fun onError(code: Int, msg: String?) {
                ct.resumeWithException(Exception(msg))
            }

            override fun onSuccess(data: List<QLiveRoomInfo>?) {
                ct.resume(data ?: ArrayList<QLiveRoomInfo>())
            }
        })
    }

    private fun load(page: Int) {
        bg {
            doWork {
                val data = suspendLoad(page).filter {
                    it.anchorStatus == 1 && it.anchor.userId != QLive.getLoginUser().userId
                }
                mSmartRecyclerView.onFetchDataFinish(data, false)
            }
            catchError {
                mSmartRecyclerView.onFetchDataError()
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
        mSmartRecyclerView.setUp(mAdapter,  true, true) {
            load(it)
        }
        mSmartRecyclerView.startRefresh()
    }
}

