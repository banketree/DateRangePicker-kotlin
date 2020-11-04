package com.banketree.daterangepicker.core

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.banketree.daterangepicker.R
import kotlinx.android.synthetic.main.widget_date_pick_item_day.view.*
import kotlinx.android.synthetic.main.widget_date_pick_item_month.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class CalendarAdapter(
    private val context: Context,
    data: ArrayList<DateBean>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    var data = ArrayList<DateBean>()
    var onRecyclerviewItemClick: OnRecyclerviewItemClick? = null

    init {
        this.data = data
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == DateBean.ITEM_TYPE_MONTH) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.widget_date_pick_item_month, parent, false)
            val monthViewHolder = MonthViewHolder(view)
            monthViewHolder.itemView.setOnClickListener { v ->
                onRecyclerviewItemClick?.onItemClick(v, monthViewHolder.layoutPosition)
            }
            return monthViewHolder
        }

        //viewType == DateBean.ITEM_TYPE_DAY
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.widget_date_pick_item_day, parent, false)
        val dayViewHolder = DayViewHolder(view)
        dayViewHolder.itemView.setOnClickListener { v ->
            onRecyclerviewItemClick?.onItemClick(v, dayViewHolder.layoutPosition)
        }
        return dayViewHolder
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (holder is MonthViewHolder) {
            holder.month_tv.text = data[position].monthStr
        } else {
            val viewHolder = holder as DayViewHolder?
            val format = SimpleDateFormat("yyyy-MM-dd")
            val todayDate = Date()
            val todayStr = format.format(todayDate) //获取今天日期
            val date =
                data[position].monthStr + "-" + data[position].day //获取得到的日期
            var beforeToday = Date()
            try {
                beforeToday = format.parse(date)
            } catch (e: ParseException) {
            }
            if (date == todayStr) {
                //如果是今天的日期  则把显示的日期号改为“今天”两个字
                viewHolder!!.day_tv.setText(R.string.today)
                viewHolder.day_tv.setTextColor(Color.parseColor("#2196F3"))
            } else if (beforeToday.time > todayDate.time) {
                //今天之后的日期  设置成灰色
                viewHolder!!.day_tv.text = data[position].day
                viewHolder.day_tv.setTextColor(Color.parseColor("#dadada"))
            } else {
                viewHolder!!.day_tv.text = data[position].day
                viewHolder.day_tv.setTextColor(Color.BLACK)
            }
            val dateBean = data[position]
            //设置item状态
            if (dateBean.itemState == DateBean.ITEM_STATE_BEGIN_DATE || dateBean.itemState == DateBean.ITEM_STATE_END_DATE) {
                //开始日期或结束日期
                viewHolder.itemView.setBackgroundColor(
                    context.resources.getColor(R.color.auxiliary_color)
                )
                viewHolder.day_tv.setTextColor(Color.WHITE)
                viewHolder.check_in_check_out_tv.visibility = View.VISIBLE
                if (dateBean.itemState == DateBean.ITEM_STATE_BEGIN_DATE) {
                    viewHolder.check_in_check_out_tv.setText(R.string.start)
                } else {
                    viewHolder.check_in_check_out_tv.setText(R.string.end)
                }
            } else if (dateBean.itemState == DateBean.ITEM_STATE_SELECTED) {
                //选中状态
                viewHolder.itemView.setBackgroundColor(
                    context.resources.getColor(R.color.auxiliary_color_30)
                )
                viewHolder.day_tv.setTextColor(Color.WHITE)
            } else {
                //正常状态
                viewHolder.itemView.setBackgroundColor(Color.WHITE)
                viewHolder.check_in_check_out_tv.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].itemType
    }

    inner class DayViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var day_tv: TextView = itemView.day_tv
        var check_in_check_out_tv: TextView = itemView.check_in_check_out_tv
    }

    inner class MonthViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var month_tv: TextView = itemView.month_tv
    }

    interface OnRecyclerviewItemClick {
        fun onItemClick(v: View?, position: Int)
    }
}
