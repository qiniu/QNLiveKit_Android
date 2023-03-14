package com.qlive.uikitlinkmic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;

import com.qlive.avparam.QMixStreaming;
import com.qlive.avparam.QPlayerEventListener;
import com.qlive.avparam.QRoomConnectionState;
import com.qlive.core.QClientType;
import com.qlive.core.been.QExtension;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.core.been.QLiveUser;
import com.qlive.linkmicservice.QMicLinker;
import com.qlive.linkmicservice.QAudienceMicHandler;
import com.qlive.linkmicservice.QLinkMicMixStreamAdapter;
import com.qlive.linkmicservice.QLinkMicService;
import com.qlive.linkmicservice.QLinkMicServiceListener;
import com.qlive.playerclient.QPlayerClient;
import com.qlive.rtclive.QPushTextureView;
import com.qlive.uikitcore.QKitFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 两人分屏连麦demo
 */
public class MicLinkerSplitScreenPreview extends QKitFrameLayout {

    private BaseSplitScreenPreview linkerPreview = null;

    public MicLinkerSplitScreenPreview(@NonNull Context context) {
        super(context);
    }

    public MicLinkerSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MicLinkerSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutId() {
        return -1;
    }

    @Override
    public void initView() {
        if (Objects.requireNonNull(getClient()).getClientType() == QClientType.PLAYER) {
            linkerPreview = new PlayerMicLinkerSplitScreenPreview(getContext());
        } else {
            linkerPreview = new AnchorMicLinkerSplitScreenPreview(getContext());
        }
        addView(linkerPreview, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linkerPreview.attachKitContext(Objects.requireNonNull(getKitContext()));
        linkerPreview.attachLiveClient(getClient());
    }

    @Override
    public void onEntering(@NonNull String roomId, @NonNull QLiveUser user) {
        super.onEntering(roomId, user);
        linkerPreview.onEntering(roomId, user);
    }

    @Override
    public void onJoined(@NonNull QLiveRoomInfo roomInfo, boolean isJoinedBefore) {
        super.onJoined(roomInfo, isJoinedBefore);
        linkerPreview.onJoined(roomInfo, isJoinedBefore);
    }

    @Override
    public void onLeft() {
        super.onLeft();
        linkerPreview.onLeft();
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();
        linkerPreview.onDestroyed();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        super.onStateChanged(source, event);
        linkerPreview.onStateChanged(source, event);
    }

    static class BaseSplitScreenPreview extends QKitFrameLayout {

        protected FrameLayout flAnchorContainer = null;
        protected FrameLayout flAudienceContainer = null;
        protected ImageView ivAnchorAvatar = null;
        protected TextView tvAnchorName = null;
        protected ImageView ivAudienceAvatar = null;
        protected TextView ivAudienceName = null;
        protected View llAnchorCover = null;
        protected View llAudienceCover = null;
        protected FrameLayout flVideoContainer = null;
        protected ImageView ivBackGround;

        public BaseSplitScreenPreview(@NonNull Context context) {
            super(context);
        }

        public BaseSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public BaseSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public int getLayoutId() {
            return R.layout.kit_mic_linker_slplit_prew;
        }

        @Override
        public void initView() {
            flAnchorContainer = findViewById(R.id.flAnchorContainer);
            flAudienceContainer = findViewById(R.id.flAudienceContainer);
            ivAnchorAvatar = findViewById(R.id.ivAnchorAvatar);
            tvAnchorName = findViewById(R.id.tvAnchorName);
            ivAudienceAvatar = findViewById(R.id.ivAudienceAvatar);
            ivAudienceName = findViewById(R.id.ivAudienceName);
            llAnchorCover = findViewById(R.id.llAnchorCover);
            llAudienceCover = findViewById(R.id.llAudienceCover);
            flVideoContainer = findViewById(R.id.flVideoContainer);
            ivBackGround = findViewById(R.id.ivBackGround);
        }
    }

    /**
     * 主播端分屏连麦逻辑
     */
    static class AnchorMicLinkerSplitScreenPreview extends BaseSplitScreenPreview {

        public AnchorMicLinkerSplitScreenPreview(@NonNull Context context) {
            super(context);
        }

        public AnchorMicLinkerSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public AnchorMicLinkerSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        /**
         * 混流适配
         */
        private final QLinkMicMixStreamAdapter linkMicMixStreamAdapter = new QLinkMicMixStreamAdapter() {
            @Override
            public QMixStreaming.MixStreamParams onMixStreamStart() {
                //连麦开始 双人连麦 混流分辨率 720 ，419 ->
                QMixStreaming.MixStreamParams params = new QMixStreaming.MixStreamParams();
                params.mixStreamWidth = (720);
                params.mixStringHeight = (419);
                params.mixBitrate = (1500);
                params.FPS = (25);
                return params;
            }

            @Override
            public List<QMixStreaming.MergeOption> onResetMixParam(List<QMicLinker> list, QMicLinker qMicLinker, boolean b) {
                QMixStreaming.MergeOption anchor = new QMixStreaming.MergeOption();

                //主播摄像头
                QMixStreaming.CameraMergeOption anchorCameraMergeOption = new QMixStreaming.CameraMergeOption();
                anchorCameraMergeOption.width = (720 / 2);
                anchorCameraMergeOption.height = (419);
                anchorCameraMergeOption.isNeed = (true);
                anchorCameraMergeOption.x = (0);
                anchorCameraMergeOption.y = (0);
                anchorCameraMergeOption.z = (0);
                //主播麦克风
                QMixStreaming.MicrophoneMergeOption anchorMicrophoneMergeOption = new QMixStreaming.MicrophoneMergeOption();
                anchorMicrophoneMergeOption.isNeed = (true);
                //主播
                anchor.cameraMergeOption = (anchorCameraMergeOption);
                anchor.microphoneMergeOption = (anchorMicrophoneMergeOption);
                anchor.uid = (list.get(0).user.userId);


                QMixStreaming.MergeOption player = new QMixStreaming.MergeOption();
                //观众摄像头
                QMixStreaming.CameraMergeOption playerCameraMergeOption = new QMixStreaming.CameraMergeOption();
                playerCameraMergeOption.width = (720 / 2);
                playerCameraMergeOption.height = (419);
                playerCameraMergeOption.isNeed = (true);
                playerCameraMergeOption.x = (720 / 2);
                playerCameraMergeOption.y = (0);
                playerCameraMergeOption.z = (0);
                //观众麦克风
                QMixStreaming.MicrophoneMergeOption playerMicrophoneMergeOption = new QMixStreaming.MicrophoneMergeOption();
                playerMicrophoneMergeOption.isNeed = (true);
                //观众
                player.cameraMergeOption = (playerCameraMergeOption);
                player.microphoneMergeOption = (playerMicrophoneMergeOption);
                player.uid = (list.get(1).user.userId);

                List<QMixStreaming.MergeOption> mergeOptions = new ArrayList<>();
                mergeOptions.add(anchor);
                mergeOptions.add(player);
                return mergeOptions;
            }
        };


        /**
         * 原来主播的预览父容器
         */
        private ViewGroup originAnchorPreViewParent = null;
        private int originAnchorPreviewIndex = -1;
        /**
         * 麦位监听
         */
        private final QLinkMicServiceListener micServiceListener = new QLinkMicServiceListener() {
            @Override
            public void onLinkerJoin(QMicLinker qMicLinker) {
                //有人上麦
                //当前房间是主播
                //主播原来自己的预览
                View originMyPreView = Objects.requireNonNull(getKitContext()).getGetPusherRenderViewCall().invoke().getView();
                //原来主播的预览父容器
                originAnchorPreViewParent = (ViewGroup) originMyPreView.getParent();
                originAnchorPreviewIndex = originAnchorPreViewParent.indexOfChild(originMyPreView);

                //主播自己的窗口变小  这里可以加点特效
                originAnchorPreViewParent.removeView(originMyPreView);
                flAnchorContainer.addView(originMyPreView, 0);

                //主播设置对方预览
                QPushTextureView playerPreview = new QPushTextureView(getContext());
                flAudienceContainer.addView(playerPreview, 0);
                Objects.requireNonNull(getClient()).getService(QLinkMicService.class).setUserPreview(qMicLinker.user.userId, playerPreview);

                //设置麦位头像名字
                Glide.with(getContext()).load(Objects.requireNonNull(getRoomInfo()).anchor.avatar).into(ivAnchorAvatar);
                tvAnchorName.setText(getRoomInfo().anchor.nick);
                Glide.with(getContext()).load(qMicLinker.user.avatar).into(ivAudienceAvatar);
                ivAudienceName.setText(qMicLinker.user.nick);
                llAnchorCover.setVisibility(View.VISIBLE);
                llAudienceCover.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLinkerLeft(@NonNull QMicLinker qMicLinker) {
                //当前房间是主播
                flAnchorContainer.removeViewAt(0);
                View originMyPreView = Objects.requireNonNull(getKitContext()).getGetPusherRenderViewCall().invoke().getView();
                originAnchorPreViewParent.addView(originMyPreView, originAnchorPreviewIndex);

                flAudienceContainer.removeViewAt(0);

                llAnchorCover.setVisibility(View.GONE);
                llAudienceCover.setVisibility(View.GONE);
            }

            @Override
            public void onLinkerMicrophoneStatusChange(@NonNull QMicLinker qMicLinker) {
            }

            @Override
            public void onLinkerCameraStatusChange(@NonNull QMicLinker qMicLinker) {
            }

            @Override
            public void onLinkerKicked(@NonNull QMicLinker qMicLinker, String s) {
                onLinkerLeft(qMicLinker);
            }

            @Override
            public void onLinkerExtensionUpdate(@NonNull QMicLinker qMicLinker, QExtension qExtension) {
            }
        };


        @Override
        public void initView() {
            super.initView();
            //添加麦位监听
            Objects.requireNonNull(getClient()).getService(QLinkMicService.class).addMicLinkerListener(micServiceListener);
            //主播监听混流适配
            getClient().getService(QLinkMicService.class).getAnchorHostMicHandler().setMixStreamAdapter(linkMicMixStreamAdapter);
        }

        @Override
        public void onDestroyed() {
            Objects.requireNonNull(getClient()).getService(QLinkMicService.class).removeMicLinkerListener(micServiceListener);
            getClient().getService(QLinkMicService.class).getAnchorHostMicHandler().setMixStreamAdapter(null);
            super.onDestroyed();

        }
    }


    /**
     * 观众端分屏连麦逻辑
     */
    static class PlayerMicLinkerSplitScreenPreview extends BaseSplitScreenPreview {

        public PlayerMicLinkerSplitScreenPreview(@NonNull Context context) {
            super(context);
        }

        public PlayerMicLinkerSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public PlayerMicLinkerSplitScreenPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        /**
         * 用户端连麦处理监听
         */
        private final QAudienceMicHandler.LinkMicHandlerListener micListener = new QAudienceMicHandler.LinkMicHandlerListener() {
            @Override
            public void onConnectionStateChanged(@NonNull QRoomConnectionState qRoomConnectionState) {
                //我的连麦链接状态
            }

            @Override
            public void onRoleChange(boolean isLinker) {
                //我的角色变化 设置原来麦上用户预览 也就是主播 双人连麦 原来麦上就只有主播
                if (isLinker) {
                    //设置主播预览
                    QPushTextureView anchorPreview = new QPushTextureView(getContext());
                    flAnchorContainer.addView(anchorPreview, 0);
                    Objects.requireNonNull(getClient()).getService(QLinkMicService.class).setUserPreview(Objects.requireNonNull(getRoomInfo()).anchor.userId, anchorPreview);

                    //设置自己的预览
                    QPushTextureView mePreview = new QPushTextureView(getContext());
                    flAudienceContainer.addView(mePreview, 0);
                    getClient().getService(QLinkMicService.class).setUserPreview(Objects.requireNonNull(getUser()).userId, mePreview);

                    ivBackGround.setVisibility(View.VISIBLE);
                } else {
                    //移除主播预览
                    flAnchorContainer.removeViewAt(0);
                    //移除自己的预览
                    flAudienceContainer.removeViewAt(0);

                    ivBackGround.setVisibility(View.GONE);
                }
            }
        };
        private boolean isLinking = false;
        /**
         * 麦位监听
         */
        private final QLinkMicServiceListener micServiceListener = new QLinkMicServiceListener() {
            @Override
            public void onLinkerJoin(QMicLinker qMicLinker) {
                isLinking = true;
                //设置麦位头像名字
                Glide.with(getContext()).load(Objects.requireNonNull(getRoomInfo()).anchor.avatar).into(ivAnchorAvatar);
                tvAnchorName.setText(getRoomInfo().anchor.nick);
                Glide.with(getContext()).load(qMicLinker.user.avatar).into(ivAudienceAvatar);
                ivAudienceName.setText(qMicLinker.user.nick);
                llAnchorCover.setVisibility(View.VISIBLE);
                llAudienceCover.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLinkerLeft(@NonNull QMicLinker qMicLinker) {
                isLinking = false;
                llAnchorCover.setVisibility(View.GONE);
                llAudienceCover.setVisibility(View.GONE);
            }

            @Override
            public void onLinkerMicrophoneStatusChange(@NonNull QMicLinker qMicLinker) {
            }

            @Override
            public void onLinkerCameraStatusChange(@NonNull QMicLinker qMicLinker) {
            }

            @Override
            public void onLinkerKicked(@NonNull QMicLinker qMicLinker, String s) {
                onLinkerLeft(qMicLinker);
            }

            @Override
            public void onLinkerExtensionUpdate(@NonNull QMicLinker qMicLinker, QExtension qExtension) {
            }
        };

        //播放器监听
        private final QPlayerEventListener playerEventListener = new QPlayerEventListener() {
            @Override
            public void onPrepared(int i) {
            }

            @Override
            public void onInfo(int i, int i1) {
            }

            @Override
            public void onBufferingUpdate(int i) {
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                if (isLinking && width > height && !isMovePlayerSmall) {
                    movePlayerSmall();
                } else if (isMovePlayerSmall) {
                    movePlayerBig();
                }
            }

            @Override
            public boolean onError(int i) {
                return false;
            }

            private boolean isMovePlayerSmall = false;

            private ViewGroup originPlayerParent = null;
            private int originPlayerIndex = 0;

            private void movePlayerSmall() {
                isMovePlayerSmall = true;
                View player = Objects.requireNonNull(getKitContext()).getGetPlayerRenderViewCall().invoke().getView();
                originPlayerParent = (ViewGroup) player.getParent();
                originPlayerIndex = originPlayerParent.indexOfChild(player);

                originPlayerParent.removeView(player);
                flVideoContainer.addView(player, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
            }

            private void movePlayerBig() {
                isMovePlayerSmall = false;
                View player = Objects.requireNonNull(getKitContext()).getGetPlayerRenderViewCall().invoke().getView();
                flVideoContainer.removeView(player);
                originPlayerParent.addView(player, originPlayerIndex, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
            }
        };

        @Override
        public void onJoined(@NonNull QLiveRoomInfo roomInfo, boolean isJoinedBefore) {
            super.onJoined(roomInfo, isJoinedBefore);
            if (isJoinedBefore) {
                //从销毁的activity 小窗恢复麦位置UI 没有小窗模式可以不加
                if (getClient().getService(QLinkMicService.class).getAllLinker().size() == 2) {
                    micServiceListener.onLinkerJoin(getClient().getService(QLinkMicService.class).getAllLinker().get(1));
                }
            }
        }

        @Override
        public void initView() {
            super.initView();
            //添加麦位监听
            Objects.requireNonNull(getClient()).getService(QLinkMicService.class).addMicLinkerListener(micServiceListener);
            //观众添加观众连麦处理
            getClient().getService(QLinkMicService.class).getAudienceMicHandler().addLinkMicListener(micListener);
            ((QPlayerClient) getClient()).addPlayerEventListener(playerEventListener);
        }

        @Override
        public void onDestroyed() {
            Objects.requireNonNull(getClient()).getService(QLinkMicService.class).removeMicLinkerListener(micServiceListener);
            //观众添加观众连麦处理
            getClient().getService(QLinkMicService.class).getAudienceMicHandler().removeLinkMicListener(micListener);
            ((QPlayerClient) getClient()).removePlayerEventListener(playerEventListener);
            super.onDestroyed();
        }
    }
}
