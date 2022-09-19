package com.qlive.uikitcore.adapter

import android.content.Context
import android.view.ViewGroup

abstract class BaseItemProvider<T> {
    var mContext: Context? = null
    var mData: List<T>? = null

    //子类须重写该方法返回layout
    //Rewrite this method to return layout
    abstract fun layout(): Int
    abstract fun convert(helper: QRecyclerViewHolder?, data: T, position: Int)

    //子类若想实现条目点击事件则重写该方法
    //Subclasses override this method if you want to implement an item click event
    fun onClick(helper: QRecyclerViewHolder?, data: T, position: Int) {}

    //子类若想实现条目长按事件则重写该方法
    //Subclasses override this method if you want to implement an item long press event
    fun onLongClick(helper: QRecyclerViewHolder?, data: T, position: Int): Boolean {
        return false
    }
}

abstract class QMultipleItemRvAdapter<T>(data: List<T>) : QRecyclerAdapter<T>(data) {
    protected val itemProvider = HashMap<Int, BaseItemProvider<T>>()
    protected abstract fun getViewType(t: T): Int
    abstract fun registerItemProvider()

    override fun initAdapter() {
        super.initAdapter()
        registerItemProvider()
    }

    override fun convert(helper: QRecyclerViewHolder, item: T) {
        val itemViewType = helper.itemViewType
        val provider = itemProvider[itemViewType]!!
        provider.mContext = helper.itemView.context
        provider.mData = mData
        val position = helper.layoutPosition - headerLayoutCount
        provider.convert(helper, item, position)
        bindClick(helper, item, position, provider)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): QRecyclerViewHolder {
        val layoutId = itemProvider[viewType]!!.layout()
        return createBaseViewHolder(parent, layoutId)
    }

    override fun getDefItemViewType(position: Int): Int {
        return getViewType(data[position])
    }

    private fun bindClick(
        helper: QRecyclerViewHolder,
        item: T,
        position: Int,
        provider: BaseItemProvider<T>
    ) {
        val clickListener = onItemClickListener
        val longClickListener = onItemLongClickListener
        if (clickListener != null && longClickListener != null) {
            //如果已经设置了子条目点击监听和子条目长按监听
            // If you have set up a sub-entry click monitor and sub-entries long press listen
            return
        }
        val itemView = helper.itemView
        if (clickListener == null) {
            //如果没有设置点击监听，则回调给itemProvider
            //Callback to itemProvider if no click listener is set
            itemView.setOnClickListener { provider.onClick(helper, item, position) }
        }
        if (longClickListener == null) {
            //如果没有设置长按监听，则回调给itemProvider
            // If you do not set a long press listener, callback to the itemProvider
            itemView.setOnLongClickListener { provider.onLongClick(helper, item, position) }
        }
    }
}