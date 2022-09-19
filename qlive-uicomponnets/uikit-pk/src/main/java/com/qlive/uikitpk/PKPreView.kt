package com.qlive.uikitpk

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qlive.avparam.*
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.rtclive.QPushTextureView
import com.qlive.pkservice.QPKService
import com.qlive.pkservice.QPKMixStreamAdapter
import com.qlive.pkservice.QPKServiceListener
import com.qlive.pkservice.QPKSession
import com.qlive.playerclient.QPlayerClient
import com.qlive.uikitcore.LinkerUIHelper
import com.qlive.uikitcore.QKitFrameLayout
import kotlinx.android.synthetic.main.kit_anchor_pk_preview.view.*

//观众端pk预览
class PKPlayerPreview : QKitFrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var isPKingPreview = false
    private var isPKing = false
    private val mQPKServiceListener = object :
        QPKServiceListener {

        override fun onStart(pkSession: QPKSession) {
            isPKing = true
        }

        override fun onStop(pkSession: QPKSession, code: Int, msg: String) {
            isPKing = false
        }

        override fun onStartTimeOut(pkSession: QPKSession) {}
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        if (isResumeUIFromFloating) {
            client!!.getService(QPKService::class.java).currentPKingSession()?.let {
                mQPKServiceListener.onStart(it)
            }
        }
    }

    private var mQPlayerEventListener = object : QPlayerEventListener {
        override fun onPrepared(preparedTime: Int) {}
        override fun onInfo(what: Int, extra: Int) {}
        override fun onBufferingUpdate(percent: Int) {}

        /**
         * 混流变化了 把播放器缩小
         */
        override fun onVideoSizeChanged(width: Int, height: Int) {
            if (width < height && isPKingPreview) {
                removeView()
            } else if (isPKing && !isPKingPreview && width > height) {
                addView()
            }
        }

        override fun onError(errorCode: Int): Boolean {
            return true
        }
    }

    private var originParent: ViewGroup? = null
    private var originIndex = 0
    private fun addView() {
        isPKingPreview = true
        val player = kitContext?.getPlayerRenderViewCall?.invoke()?.getView() ?: return
        val parent = player.parent as ViewGroup
        originParent = parent
        originIndex = parent.indexOfChild(player)
        parent.removeView(player)
        llPKContainer.addView(
            player,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        )
    }

    private fun removeView() {
        isPKingPreview = false
        val rendView = kitContext?.getPlayerRenderViewCall?.invoke()
        val player = rendView?.getView() ?: return
        llPKContainer.removeView(player)
        originParent?.addView(
            player,
            originIndex,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        player.requestLayout()
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_anchor_pk_preview
    }

    override fun initView() {
        client!!.getService(QPKService::class.java).addServiceListener(mQPKServiceListener)
        (client as QPlayerClient).addPlayerEventListener(mQPlayerEventListener)
    }

    override fun onDestroyed() {
        client!!.getService(QPKService::class.java).removeServiceListener(mQPKServiceListener)
        (client as QPlayerClient).removePlayerEventListener(mQPlayerEventListener)
        super.onDestroyed()
    }
}


//主播端预览
class PKAnchorPreview : QKitFrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    //混流适配
    private val mQPKMixStreamAdapter = object : QPKMixStreamAdapter {

        override fun onPKLinkerJoin(pkSession: QPKSession): MutableList<QMixStreaming.MergeOption> {
            return LinkerUIHelper.getPKMixOp(pkSession, user!!)
        }

        override fun onPKMixStreamStart(pkSession: QPKSession): QMixStreaming.MixStreamParams? {
            return QMixStreaming.MixStreamParams().apply {
                mixStreamWidth = LinkerUIHelper.pkMixWidth
                mixStringHeight = LinkerUIHelper.pkMixHeight
                mixBitrate = QMixStreaming.MixStreamParams.DEFAULT_BITRATE
                FPS = 25
            }
        }
    }

    private var localRenderView: View? = null

    //PK监听
    private val mQPKServiceListener = object :
        QPKServiceListener {

        override fun onStart(pkSession: QPKSession) {
            val peer = if (pkSession.initiator.userId == user?.userId) {
                pkSession.receiver
            } else {
                pkSession.initiator
            }
            //我自己预览缩小
            changeMeRenderViewToPk()
            //添加对方预览
            flPeerContainer.addView(
                QPushTextureView(context).apply {
                    client?.getService(QPKService::class.java)
                        ?.setPeerAnchorPreView(this)
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        override fun onStop(pkSession: QPKSession, code: Int, msg: String) {
            //我自己预览放大
            changeMeRenderViewToStopPk()
            //移除对方主播预览
            flPeerContainer.removeAllViews()
        }

        override fun onStartTimeOut(pkSession: QPKSession) {}

    }

    override fun getLayoutId(): Int {
        return R.layout.kit_anchor_pk_preview
    }

    override fun initView() {
        client!!.getService(QPKService::class.java).addServiceListener(mQPKServiceListener)
        client!!.getService(QPKService::class.java).setPKMixStreamAdapter(mQPKMixStreamAdapter)
    }

    override fun onDestroyed() {
        client!!.getService(QPKService::class.java).removeServiceListener(mQPKServiceListener)
        client!!.getService(QPKService::class.java).setPKMixStreamAdapter(null)
        super.onDestroyed()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)

    }

    /**
     * 缩小动画
     */
    private var pkScaleX = 0f
    private var pkScaleY = 0f
    private var originPreViewParent: ViewGroup? = null
    private var originIndex = -1;
    private fun changeMeRenderViewToPk() {
        localRenderView = kitContext?.getPusherRenderViewCall?.invoke()?.getView() ?: return

        pkScaleX = (flMeContainer.width / localRenderView!!.width.toFloat())
        pkScaleY = flMeContainer.height / (localRenderView!!.height.toFloat())
//        localRenderView!!.pivotX = 0f
//        localRenderView!!.pivotY = localRenderView!!.height / 2f
        val scaleX = ObjectAnimator.ofFloat(
            localRenderView!!,
            "scaleX",
            1f, pkScaleX
        )
        scaleX.duration = 500
        scaleX.repeatCount = 0
        val scaleY = ObjectAnimator.ofFloat(
            localRenderView!!,
            "scaleY",
            1f,
            pkScaleY
        )
        scaleY.duration = 500
        scaleY.repeatCount = 0
        val translationY = ObjectAnimator.ofFloat(
            localRenderView!!,
            "translationY",
            0f,
            -(localRenderView!!.height / 2f - (flMeContainer.height / 2f + llPKContainer.y))
        )
        translationY.duration = 500
        translationY.repeatCount = 0
        val translationX = ObjectAnimator.ofFloat(
            localRenderView!!,
            "translationX",
            0f,
            -(localRenderView!!.width / 2f - (flMeContainer.width / 2f))
        )
        translationX.duration = 500
        translationX.repeatCount = 0
        AnimatorSet().apply {
            play(scaleX).with(scaleY).with(translationY).with(translationX)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {}

                override fun onAnimationEnd(p0: Animator?) {
                    originPreViewParent = localRenderView!!.parent as ViewGroup
                    originIndex = originPreViewParent?.indexOfChild(localRenderView) ?: 0
                    originPreViewParent?.removeView(localRenderView)
                    flMeContainer.addView(localRenderView)
                    flPeerContainer.addView(
                        QPushTextureView(context).apply {
                            client?.getService(QPKService::class.java)
                                ?.setPeerAnchorPreView(this)
                        },
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    localRenderView!!.scaleX = 1f
                    localRenderView!!.scaleY = 1f
                    localRenderView!!.translationY = 0f
                    localRenderView!!.translationX = 0f
                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationRepeat(p0: Animator?) {}
            })
        }.start()
    }

    /**
     * 放大我的预览
     */
    private fun changeMeRenderViewToStopPk() {
        localRenderView = kitContext?.getPusherRenderViewCall?.invoke()?.getView() ?: return
        flMeContainer.removeView(localRenderView)
        originPreViewParent?.addView(
            localRenderView, originIndex, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        flPeerContainer.removeAllViews()
    }

}
