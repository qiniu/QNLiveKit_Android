package com.qlive.uikitshopping

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.shoppingservice.QItem
import com.qlive.shoppingservice.QItemStatus
import com.qlive.shoppingservice.QShoppingService
import com.qlive.shoppingservice.QSingleOrderParam
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.backGround
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.bg
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikitcore.smartrecycler.SmartRecyclerView
import com.qlive.uikitcore.view.CommonViewPagerAdapter
import com.qlive.uikitshopping.databinding.KitDialogShoppingManagerBinding
import com.qlive.uikitshopping.databinding.KitItemManagerGoodsBinding
import com.qlive.uikitcore.view.flowlayout.FlowLayout
import com.qlive.uikitcore.view.flowlayout.TagAdapter

import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ShoppingManagerDialog(
    private val kitContext: QLiveUIKitContext,
    private val client: QLiveClient
) : ViewBindingDialogFragment<KitDialogShoppingManagerBinding>() {

    private val views by lazy {
        listOf<ShoppingManagerPage>(
            ShoppingManagerPage(requireContext()).apply {
                filterStatus = -1

            },
            ShoppingManagerPage(requireContext()).apply {
                filterStatus = QItemStatus.ON_SALE.value
            },
            ShoppingManagerPage(requireContext()).apply {
                filterStatus = QItemStatus.PULLED.value
            }
        )
    }
    private val shoppingService get() = client.getService(QShoppingService::class.java)!!

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    @SuppressLint("SetTextI18n")
    private fun setCount(filterStatus: Int, count: Int) {
        when (filterStatus) {
            -1 -> binding.rbAll.text = getString(R.string.shopping_dialog_goods_manager_all, count)
            QItemStatus.ON_SALE.value -> binding.rbOnSale.text =
                getString(R.string.shopping_dialog_goods_manager_onsale, count)
            QItemStatus.PULLED.value -> binding.rbPulled.text =
                getString(R.string.shopping_dialog_goods_manager_pulled, count)
        }
    }

    private suspend fun getList() = suspendCoroutine<List<QItem>> { coroutine ->
        shoppingService.getItemList(object : QLiveCallBack<List<QItem>> {
            override fun onError(code: Int, msg: String?) {
                coroutine.resumeWithException(Exception(msg))
            }

            override fun onSuccess(data: List<QItem>) {
                coroutine.resume(data)
                setCount(-1, data.size)
                val onSaleList = data.filter {
                    (it.status == QItemStatus.ON_SALE.value || it.status == QItemStatus.ONLY_DISPLAY.value)
                }
                setCount(QItemStatus.ON_SALE.value, onSaleList.size)
                val pulledList = data.filter {
                    (it.status == QItemStatus.PULLED.value)
                }
                setCount(QItemStatus.PULLED.value, pulledList.size)
            }
        })
    }

    private suspend fun deleteItems(itemIDS: List<String>) =
        suspendCoroutine<Unit> { coroutine ->
            shoppingService.deleteItems(itemIDS, object : QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    coroutine.resumeWithException(Exception(msg))
                }

                override fun onSuccess(data: Void?) {
                    coroutine.resume(Unit)
                }
            })
        }

    private suspend fun changeUpdateStatus(newStatus: java.util.HashMap<String, QItemStatus>) =
        suspendCoroutine<Unit> { coroutine ->
            shoppingService.updateItemStatus(newStatus, object : QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    coroutine.resumeWithException(Exception(msg))
                }

                override fun onSuccess(data: Void?) {
                    coroutine.resume(Unit)
                }
            })
        }

    private suspend fun moveItem(param: QSingleOrderParam) = suspendCoroutine<Unit> { coroutine ->
        shoppingService.changeSingleOrder(param, object : QLiveCallBack<Void> {
            override fun onError(code: Int, msg: String?) {
                coroutine.resumeWithException(Exception(msg))
            }

            override fun onSuccess(data: Void?) {
                coroutine.resume(Unit)
            }
        })
    }

    override fun init() {
        binding.vpGoods.adapter = CommonViewPagerAdapter(views)
        binding.radioGroup.setOnCheckedChangeListener { p0, id ->
            when (id) {
                R.id.rbAll -> {
                    if (binding.vpGoods.currentItem != 0) {
                        binding.vpGoods.currentItem = 0
                    }
                }
                R.id.rbOnSale -> {
                    if (binding.vpGoods.currentItem != 1) {
                        binding.vpGoods.currentItem = 1
                    }
                }
                R.id.rbPulled -> {
                    if (binding.vpGoods.currentItem != 2) {
                        binding.vpGoods.currentItem = 2
                    }
                }
            }
        }

        binding.vpGoods.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.tvUp.isClickable = true
                        binding.tvDown.isClickable = true
                        binding.llMove.isClickable = true
                        binding.tvUp.isSelected = true
                        binding.tvDown.isSelected = true
                        binding.tvMove.isSelected = true

                        binding.radioGroup.check(R.id.rbAll)
                    }
                    1 -> {
                        binding.tvUp.isClickable = false
                        binding.tvDown.isClickable = true
                        binding.llMove.isClickable = true
                        binding.tvUp.isSelected = false
                        binding.tvDown.isSelected = true
                        binding.tvMove.isSelected = true
                        binding.radioGroup.check(R.id.rbOnSale)
                    }
                    2 -> {
                        binding.tvUp.isClickable = true
                        binding.tvDown.isClickable = false
                        binding.llMove.isClickable = true
                        binding.tvUp.isSelected = true
                        binding.tvDown.isSelected = false
                        binding.tvMove.isSelected = true
                        binding.radioGroup.check(R.id.rbPulled)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        binding.tvUp.isClickable = true
        binding.tvDown.isClickable = true
        binding.llMove.isClickable = true
        binding.tvUp.isSelected = true
        binding.tvDown.isSelected = true
        binding.tvMove.isSelected = true

        binding.tvUp.setDoubleCheckClickListener {
            optionList(
                getString(R.string.shopping_is_confirm_sale),
                binding.tvUp,
                views[binding.vpGoods.currentItem].selectedSet
            )
        }
        binding.tvDown.setDoubleCheckClickListener {
            optionList(
                getString(R.string.shopping_is_confirm_pulled),
                binding.tvDown,
                views[binding.vpGoods.currentItem].selectedSet
            )
        }
        binding.llMove.setDoubleCheckClickListener {
            optionList(
                getString(R.string.shopping_is_confirm_remove),
                binding.llMove,
                views[binding.vpGoods.currentItem].selectedSet
            )
        }
        views.forEach {
            it.post {
                it.startRefresh()
            }
        }
    }

    private fun showTip(tip: String, call: () -> Unit) {
        CommonTipDialog.TipBuild()
            .setTittle(tip)
            .setListener(object : BaseDialogListener() {
                override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                    super.onDialogPositiveClick(dialog, any)
                    call.invoke()
                }
            }).build("ShoppingManagerDialog_manager").show(childFragmentManager, "")
    }

    private fun optionList(tip: String, optionId: View, selectedList: HashSet<String>) {
        if (selectedList.isEmpty()) {
            return
        }
        showTip(tip) {
            bg {
                LoadingDialog.showLoading(childFragmentManager)
                doWork {
                    if (optionId == binding.llMove) {
                        deleteItems(selectedList.toList())
                        resetAll()
                        return@doWork
                    }
                    if (optionId == binding.tvDown) {
                        changeUpdateStatus(HashMap<String, QItemStatus>().apply {
                            selectedList.forEach {
                                put(it, QItemStatus.PULLED)
                            }
                        })
                        resetAll()
                        return@doWork
                    }
                    if (optionId == binding.tvUp) {
                        changeUpdateStatus(HashMap<String, QItemStatus>().apply {
                            selectedList.forEach {
                                put(it, QItemStatus.ON_SALE)
                            }
                        })
                        resetAll()
                        return@doWork
                    }
                }
                catchError {
                    it.message?.asToast(requireContext())
                }
                onFinally {
                    LoadingDialog.cancelLoadingDialog()
                }
            }
        }
    }

    private fun resetAll() {
        views.forEach {
            it.reset()
        }
    }


    inner class ShoppingManagerPage : SmartRecyclerView {
        val selectedSet = HashSet<String>()
        private val mAdapter = ShoppingManagerPageAdapter()
        var filterStatus = -1
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            private var start: QItem? = null
            private var end: QItem? = null;

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (recyclerView.layoutManager is GridLayoutManager) {
                    val dragFlags =
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    val swipeFlags = 0
                    makeMovementFlags(dragFlags, swipeFlags)
                } else {
                    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    val swipeFlags = 0
                    makeMovementFlags(dragFlags, swipeFlags)
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (start == null) {
                    start = mAdapter.data[fromPosition]
                }
                end = mAdapter.data[toPosition]
                Log.d(
                    "ShoppingManagerPage",
                    "ShoppingManagerPage ${fromPosition}   ${toPosition}   "
                )
                mAdapter.move(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun onSelectedChanged(
                viewHolder: RecyclerView.ViewHolder?,
                actionState: Int
            ) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder?.itemView?.alpha = 0.5f;
                    smartRefreshLayout.isEnabled = false
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                if (start != null && end != null) {
                    bg {
                        LoadingDialog.showLoading(childFragmentManager)
                        doWork {
                            moveItem(QSingleOrderParam().apply {
                                itemID = start!!.itemID
                                from = start!!.order
                                to = end!!.order
                            })
                        }
                        catchError {
                            it.message?.asToast(requireContext())
                        }
                        onFinally {
                            start = null
                            end = null
                            LoadingDialog.cancelLoadingDialog()
                            resetAll()
                        }
                    }
                } else {
                    start = null
                    end = null
                }
                Log.d("ShoppingManagerPage", "结束 ${start}   ${end}   ")
                viewHolder.itemView.alpha = 1f;
                smartRefreshLayout.isEnabled = true
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }
        })

        constructor(context: Context) : this(context, null)
        constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            setUp(mAdapter, false, true) {
                refresh()
            }

            itemTouchHelper.attachToRecyclerView(recyclerView)
        }

        fun reset() {
            selectedSet.clear()
            refresh()
        }

        private fun refresh() {
            backGround {
                doWork {
                    val list = getList().filter {
                        if (filterStatus == -1) {
                            true
                        } else if (filterStatus == QItemStatus.ON_SALE.value) {
                            (it.status == QItemStatus.ON_SALE.value || it.status == QItemStatus.ONLY_DISPLAY.value)
                        } else {
                            it.status == filterStatus
                        }
                    }
                    onFetchDataFinish(list, false)
                }
                catchError {
                    onFetchDataError()
                }
                onFinally {
                }
            }
        }

        inner class ShoppingManagerPageAdapter :
            QSmartViewBindAdapter<QItem, KitItemManagerGoodsBinding>() {
            fun move(fromPosition: Int, toPosition: Int) {
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(data, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(data, i, i - 1)
                    }
                }
                notifyItemMoved(fromPosition, toPosition)
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun convertViewBindHolder(
                helper: QRecyclerViewBindHolder<KitItemManagerGoodsBinding>,
                item: QItem
            ) {
                helper.binding.ivSort.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(helper)
                    }
                    true
                }
                Glide.with(context)
                    .load(item.thumbnail)
                    .into(helper.binding.ivCover)
                helper.binding.mAutoVoiceWaveView.attach(kitContext.lifecycleOwner)
                if (shoppingService.explaining?.itemID == item.itemID) {
                    helper.binding.llItemShowing.visibility = View.VISIBLE
                    helper.binding.mAutoVoiceWaveView.setAutoPlay(true)
                    helper.itemView.isEnabled = false
                    helper.binding.opCheckbox.isClickable = false
                    helper.binding.opCheckbox.visibility = View.INVISIBLE
                } else {
                    helper.itemView.isEnabled = true
                    helper.binding.opCheckbox.isClickable = true
                    helper.binding.llItemShowing.visibility = View.GONE
                    helper.binding.mAutoVoiceWaveView.setAutoPlay(false)
                    helper.binding.opCheckbox.visibility = View.VISIBLE
                }
                helper.binding.tvOrder.text = item.order.toString()
                helper.binding.tvGoodsName.text = item.title
                helper.binding.flGoodsTag.adapter =
                    object : TagAdapter<TagItem>(
                        TagItem.strToTagItem(
                            item.tags
                        )
                    ) {
                        override fun getView(parent: FlowLayout, position: Int, t: TagItem): View {
                            val v = LayoutInflater.from(context)
                                .inflate(R.layout.kit_item_goods_tag, parent, false)
                            (v as TextView).text = t.tagStr
                            v.setBackgroundResource(t.color)
                            return v
                        }
                    }
                helper.binding.tvNowPrice.text = item.currentPrice
                helper.binding.tvOriginPrice.text = item.originPrice
                helper.binding.tvOriginPrice.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG;
                if (item.status == QItemStatus.PULLED.value) {
                    helper.binding.tvPulledCover.visibility = View.VISIBLE
                }
                if (item.status == QItemStatus.ON_SALE.value || item.status == QItemStatus.ONLY_DISPLAY.value) {
                    //已经上架
                    helper.binding.tvPulledCover.visibility = View.GONE
                }
                helper.binding.opCheckbox.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        selectedSet.add(item.itemID)
                    } else {
                        selectedSet.remove(item.itemID)
                    }
                }
                helper.binding.opCheckbox.isChecked = selectedSet.contains(item.itemID)
            }

        }
    }
}