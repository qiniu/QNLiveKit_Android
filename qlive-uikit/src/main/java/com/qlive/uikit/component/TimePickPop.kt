package com.qlive.uikit.component

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode
import com.github.gzuliyujiang.wheelpicker.contract.DateFormatter
import com.github.gzuliyujiang.wheelpicker.contract.TimeFormatter
import com.github.gzuliyujiang.wheelpicker.widget.DatimeWheelLayout
import com.qlive.uikit.R
import com.qlive.uikitcore.ext.ViewUtil
import java.util.*


class TimePickPop(context: Context) : PopupWindow(context) {
    var onTimeSelectedCall: (time: Long, format: String) -> Unit = { _, _ -> }
    fun show(attachView: View) {
        val xOffset = (attachView.width - ViewUtil.dip2px(315f)) / 2
        showAsDropDown(attachView, xOffset, 0)
    }

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.pop_view_timer_pick, null, false)
        val wheelPicker = view.findViewById<DatimeWheelLayout>(R.id.wheelPicker)
        view.findViewById<View>(R.id.tvCancel).setOnClickListener {
            dismiss()
        }
        view.findViewById<View>(R.id.tvOK).setOnClickListener {

            val year: Int = wheelPicker.selectedYear
            val month: Int = wheelPicker.selectedMonth
            val day: Int = wheelPicker.selectedDay
            val hour: Int = wheelPicker.selectedHour
            val minute: Int = wheelPicker.selectedMinute
            val second: Int = 0

            val c1: Calendar = Calendar.getInstance()
            c1.set(year, month-1, day, hour, minute)
            onTimeSelectedCall.invoke(c1.time.time, "$year-$month-$day $hour-$minute")
            dismiss()
        }

        width = ViewUtil.dip2px(315f)
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        contentView = view
        isOutsideTouchable = true

        setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))

        wheelPicker.apply {
            dateWheelLayout.setResetWhenLinkage(false)
            timeWheelLayout.setResetWhenLinkage(false)
            yearLabelView.visibility = View.GONE
            monthLabelView.visibility = View.GONE
            dayLabelView.visibility = View.GONE
            hourLabelView.visibility = View.GONE
            minuteLabelView.visibility = View.GONE

            setTimeMode(TimeMode.HOUR_24_NO_SECOND)

            yearWheelView.textSize = ViewUtil.sp2px(14f).toFloat()
            yearWheelView.selectedTextSize = ViewUtil.sp2px(14f).toFloat()
            yearWheelView.selectedTextColor = Color.parseColor("#E6000000")
            monthWheelView.textSize = ViewUtil.sp2px(15f).toFloat()
            monthWheelView.selectedTextSize = ViewUtil.sp2px(15f).toFloat()

            dayWheelView.textSize = ViewUtil.sp2px(15f).toFloat()
            dayWheelView.selectedTextSize = ViewUtil.sp2px(15f).toFloat()

            hourWheelView.textSize = ViewUtil.sp2px(15f).toFloat()
            hourWheelView.selectedTextSize = ViewUtil.sp2px(15f).toFloat()

            minuteWheelView.textSize = ViewUtil.sp2px(15f).toFloat()
            minuteWheelView.selectedTextSize = ViewUtil.sp2px(15f).toFloat()

            setDateFormatter(object : DateFormatter {
                override fun formatYear(year: Int): String {
                    return "${year}年"
                }

                override fun formatMonth(month: Int): String {
                    return "${month}月"
                }

                override fun formatDay(day: Int): String {
                    return "${day}日"
                }
            })
            setTimeFormatter(object : TimeFormatter {
                override fun formatHour(hour: Int): String {
                    return "${hour}时"
                }

                override fun formatMinute(minute: Int): String {
                    return "${minute}分"
                }

                override fun formatSecond(second: Int): String {
                    return "${second}秒"
                }
            })
        }
    }
}
