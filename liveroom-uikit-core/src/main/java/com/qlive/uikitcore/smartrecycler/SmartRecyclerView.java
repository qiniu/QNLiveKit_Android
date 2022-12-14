package com.qlive.uikitcore.smartrecycler;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.qlive.uikitcore.R;
import com.qlive.uikitcore.refresh.QRefreshLayout;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SmartRecyclerView extends FrameLayout {
    private SmartRefreshHelper smartRefreshHelper;
    private RecyclerView recyclerView;
    protected QRefreshLayout smartRefreshLayout;
    public CommonEmptyView emptyView;

    public QRefreshLayout getSmartRefreshLayout() {
        return smartRefreshLayout;
    }

    public SmartRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }

    public SmartRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SmartRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray styled =
                context.obtainStyledAttributes(attrs, R.styleable.SmartRecyclerView, defStyleAttr, 0);
        int emptyIcon = styled.getResourceId(
                R.styleable.SmartRecyclerView_placeholder_empty_icon,
                com.qlive.uikitcore.R.drawable.kit_pic_empty
        );
        int emptyNoNetIcon = styled.getResourceId(
                R.styleable.SmartRecyclerView_placeholder_empty_no_net_icon,
                com.qlive.uikitcore.R.drawable.kit_pic_empty_network
        );
        String emptyTip = styled.getString(R.styleable.SmartRecyclerView_placeholder_empty_tips);
        styled.recycle();

        emptyView.setEmptyIcon(emptyIcon);
        emptyView.setEmptyNoNetIcon(emptyNoNetIcon);
        if (TextUtils.isEmpty(emptyTip)) {
            emptyTip = "";
        }
        emptyView.setEmptyTips(emptyTip);
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.kit_layout_refresh_recyclerview, this, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        smartRefreshLayout = view.findViewById(R.id.refreshLayout);
        emptyView = view.findViewById(R.id.emptyView);
        addView(view);
    }

    /**
     * ?????????recyclerView
     *
     * @return
     */
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    /**
     * ????????????????????????
     */
    public void startRefresh() {
        smartRefreshHelper.refresh();
    }

    /**
     * ??????view????????????
     */
    public void onFetchDataError() {
        smartRefreshHelper.onFetchDataError();
    }

    /**
     * ???????????????smartRefreshHelper????????????????????????????????????
     *
     * @param goneIfNoData ???????????????????????????
     */
    public void onFetchDataFinish(List data, Boolean goneIfNoData) {
        smartRefreshHelper.onFetchDataFinish(data, goneIfNoData);
    }

    /**
     * ???????????????smartRefreshHelper????????????????????????????????????
     *
     * @param sureLoadMoreEnd ???????????????????????????????????????????????????????????????
     */
    public void onFetchDataFinish(List data, Boolean goneIfNoData, boolean sureLoadMoreEnd) {
        smartRefreshHelper.onFetchDataFinish(data, goneIfNoData, sureLoadMoreEnd);
    }

    /**
     * ?????????
     *
     * @param adapter    ?????????
     * @param fetcherFuc ????????????????????????0??????
     */
    public void setUp(IAdapter<?> adapter
            , Boolean loadMoreNeed, Boolean refreshNeed
            , Function1<Integer, Unit> fetcherFuc
    ) {
        adapter.bindRecycler(recyclerView);
        smartRefreshHelper = new SmartRefreshHelper(getContext(), adapter, recyclerView, smartRefreshLayout, emptyView, loadMoreNeed, refreshNeed, fetcherFuc);
    }

}
