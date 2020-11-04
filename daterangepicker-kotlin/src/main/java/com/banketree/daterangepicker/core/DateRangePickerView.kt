package com.banketree.daterangepicker.core

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.banketree.daterangepicker.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateRangePickerView : FrameLayout {
    companion object {
        const val TYPE_SINGLE: Int = 1
        const val TYPE_RANGE: Int = 2
    }

    //选中开始时间
    private var selectStartDate: DateBean? = null

    //选中结束时间
    private var selectEndDate: DateBean? = null

    private var minDate: String? = null
    private var maxDate: String? = null

    //选中监听
    var onDateSelected: OnDateSelected? = null

    private var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    //日期格式化
    private val formatYYYYMM = SimpleDateFormat("yyyy-MM")
    private lateinit var recyclerView: RecyclerView
    private var adapter: CalendarAdapter? = null
    private val localDayList = ArrayList<DateBean>()
    private var type: Int =
        TYPE_RANGE

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val calender = Calendar.getInstance()
        if (TextUtils.isEmpty(minDate)) {
            minDate = simpleDateFormat.format(calender.time)
        }
        if (TextUtils.isEmpty(maxDate)) {
            calender.add(Calendar.MONTH, 3)
            maxDate = simpleDateFormat.format(calender.time)
        }

        addView(
            LayoutInflater.from(context)
                .inflate(R.layout.widget_date_pick_item_calendar, this, false)
        )
        recyclerView = findViewById(R.id.recyclerView)
        adapter = CalendarAdapter(
            context,
            localDayList
        )
        val gridLayoutManager = GridLayoutManager(context, 7)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(i: Int): Int {
                //这个方法返回的是当前位置的 item 跨度大小
                return if (DateBean.ITEM_TYPE_MONTH == localDayList[i].itemType) {
                    7
                } else {
                    1
                }
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter

        //设置分割线
//        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(context,DividerItemDecoration.VERTICAL);
//        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context,R.drawable.animated_rotate));
//        recyclerView.addItemDecoration(dividerItemDecoration);

        //实现月份标题悬停的效果   测试
        val myItemDecoration =
            MyItemDecoration()
        recyclerView.addItemDecoration(myItemDecoration)
        adapter?.onRecyclerviewItemClick = object :
            CalendarAdapter.OnRecyclerviewItemClick {
            override fun onItemClick(v: View?, position: Int) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrowDate = calendar.time
                val date =
                    localDayList[position].monthStr + "-" + localDayList[position].day //获取得到的日期
                var clickDay = Date()
                try {
                    clickDay = simpleDateFormat.parse(date)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                if (clickDay.time > tomorrowDate.time - 1000 * 60 * 60 * 24) { //-1000*60*60*24  得到的是昨天的时间  不然今天也不可选
                    //今天之前的日期不可选
                    Toast.makeText(
                        context,
                        context.getString(R.string.cur_date_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    onClick(localDayList[position])
                }
//                Timber.i("date=$date")
            }
        }

        generateUI()
    }

    private fun onClick(dateBean: DateBean) {
        if (dateBean.itemType == DateBean.ITEM_TYPE_MONTH || TextUtils.isEmpty(dateBean.day)) {
            return
        }

        fun resetState() {
            //结束日期和开始日期都已选中
            clearState() //取消选中状态
            /**
             * 一定要先清除结束日期，再重新选择开始日期，不然会有一个bug，当开始日期和结束日期都选中的时候，如果此次点选开始日期，则选中开始日期，
             * 如果点结束日期，则全都清除了，再点结束日期没有反应，应该是结束日期变为开始日期才对
             * 因此要先清除结束位置，再重新选中开始日期
             */
            //一定要先清除结束日期，再重新选择开始日期
            selectEndDate?.itemState =
                DateBean.ITEM_STATE_NORMAL
            selectEndDate = null
            selectStartDate?.itemState =
                DateBean.ITEM_STATE_NORMAL
            selectStartDate = dateBean
            selectStartDate?.itemState =
                DateBean.ITEM_STATE_BEGIN_DATE
        }

        ////这个是在Dialog显示的情况下会用到，来判断如期是否已选完，来改变Dialog里面确定按钮的选中状态
        onDateSelected?.hasSelect(false)

        if (isTypeBySingle()) {
            //单点模式
            val dateString = simpleDateFormat.format(dateBean.date)
            onDateSelected?.selected(dateString, dateString)
            resetState()
        } else if (selectStartDate == null) {
            //如果没有选中开始日期则此次操作选中开始日期
            selectStartDate = dateBean
            dateBean.itemState =
                DateBean.ITEM_STATE_BEGIN_DATE
        } else if (selectEndDate == null) {
            //如果选中了开始日期但没有选中结束日期，本次操作选中结束日期

            //如果当前点击的结束日期跟开始日期一致 则不做操作
//            if (selectStartDate == dateBean) {
//            } else
            if (dateBean.date!!.time < selectStartDate!!.date!!.time) {
                //如果当前点选的日期小于当前选中的开始日期，则本次操作重新选中开始日期
                selectStartDate!!.itemState =
                    DateBean.ITEM_STATE_NORMAL
                selectStartDate = dateBean
                selectStartDate!!.itemState =
                    DateBean.ITEM_STATE_BEGIN_DATE
            } else {
                //当前点选的日期大于当前选中的开始日期  此次操作选中结束日期
                selectEndDate = dateBean
                selectEndDate!!.itemState =
                    DateBean.ITEM_STATE_END_DATE
                setState() //选中中间的日期

                onDateSelected?.hasSelect(true)
                onDateSelected?.selected(
                    simpleDateFormat.format(selectStartDate!!.date),
                    simpleDateFormat.format(selectEndDate!!.date)
                )
            }
        } else if (selectStartDate != null && selectEndDate != null) {
            resetState()
        }
        adapter!!.notifyDataSetChanged()
    }

    //选中中间的日期
    private fun setState() {
        if (selectEndDate != null && selectStartDate != null) {
            var start = localDayList.indexOf(selectStartDate!!)
            start += 1
            val end = localDayList.indexOf(selectEndDate!!)
            while (start < end) {
                val dateBean = localDayList[start]
                if (!TextUtils.isEmpty(dateBean.day)) {
                    dateBean.itemState =
                        DateBean.ITEM_STATE_SELECTED
                }
                start++
            }
        }
    }

    //取消选中状态
    private fun clearState() {
        if (selectEndDate != null && selectStartDate != null) {
            var start = localDayList.indexOf(selectStartDate!!)
            start += 1
            val end = localDayList.indexOf(selectEndDate!!)
            while (start < end) {
                val dateBean = localDayList[start]
                dateBean.itemState =
                    DateBean.ITEM_STATE_NORMAL
                start++
            }
        }
    }

    //生成日历数据
    private fun days(startDateStr: String, endDateStr: String): List<DateBean> {
        val dateBeanList: MutableList<DateBean> = ArrayList()
        try {
            val calendar = Calendar.getInstance()

            //--------------------------------- start 动态传值方式设置显示的日历日期区间----------------------------------------//
            //起始日期
            val startDate =
                simpleDateFormat.parse(startDateStr) ?: return emptyList()//把String转成date
            //结束日期
            val endDate = simpleDateFormat.parse(endDateStr) ?: return emptyList()//把String转成date
            //---------------------------------- end ---------------------------------------//
            calendar.time =
                startDate //上面的calendar.setTime(startDate)是设置了当前时间，但是后面calendar.add(Calendar.MONTH, 5)结束日期加了5个月，日期就延后了5个月，所以要得到当前日期，需要在此处再设置一次
//            Timber.i("startDateStr:" + startDateStr + "---------endDate:" + format.format(endDate))
            calendar[Calendar.DAY_OF_MONTH] = 1 //设置日期为1
            val monthCalendar = Calendar.getInstance()

            //按月生成日历 每行7个 最多6行 42个
            //每一行有七个日期  日 一 二 三 四 五 六 的顺序
//            Timber.i("calendar.getTimeInMillis()=" + calendar.timeInMillis + "----------endDate.getTime()=" + endDate.time)
            calendar.timeInMillis
            while (calendar.timeInMillis <= endDate.time) {
                //从当前时间开始，如果小于等于最后的时间，则增加一个月
                //月份item
                val monthDateBean =
                    DateBean()
                monthDateBean.date = calendar.time
                monthDateBean.monthStr = formatYYYYMM.format(monthDateBean.date)
                monthDateBean.itemType =
                    DateBean.ITEM_TYPE_MONTH
                dateBeanList.add(monthDateBean)

                //获取一个月结束的日期和开始日期
                monthCalendar.time = calendar.time
                monthCalendar[Calendar.DAY_OF_MONTH] = 1
                val startMonthDay = calendar.time
                monthCalendar.add(Calendar.MONTH, 1) //表示加一个月
                monthCalendar.add(Calendar.DAY_OF_MONTH, -1) //表示对日期进行减一天操作
                //从而得到当前月的最后一天
                val endMonthDay = monthCalendar.time

                //重置为本月开始
                monthCalendar[Calendar.DAY_OF_MONTH] = 1
//                Timber.i(
//                    "月份的开始日期:" + format.format(startMonthDay) + "——星期" + getWeekStr(
//                        calendar[Calendar.DAY_OF_WEEK].toString() + ""
//                    ) + "---------结束日期:" + format.format(endMonthDay)
//                )
                //从月的第一天开始，如果小于等于本月最后一天，则增加一天
                monthCalendar.timeInMillis
                while (monthCalendar.timeInMillis <= endMonthDay.time) {

                    //生成单个月的日历
                    //处理一个月开始的第一天
                    if (monthCalendar[Calendar.DAY_OF_MONTH] == 1) {
                        //看某个月第一天是周几
                        val weekDay = monthCalendar[Calendar.DAY_OF_WEEK]
//                        Timber.i("dateBeanList=" + dateBeanList.size)
//                        Timber.i("monthDateBean.getMonthStr()=" + monthDateBean.monthStr)
                        when (weekDay) {
                            1 -> {
                            }
                            2 -> addDatePlaceholder(dateBeanList, 1, monthDateBean.monthStr)
                            3 -> addDatePlaceholder(dateBeanList, 2, monthDateBean.monthStr)
                            4 -> addDatePlaceholder(dateBeanList, 3, monthDateBean.monthStr)
                            5 -> addDatePlaceholder(dateBeanList, 4, monthDateBean.monthStr)
                            6 -> addDatePlaceholder(dateBeanList, 5, monthDateBean.monthStr)
                            7 -> addDatePlaceholder(dateBeanList, 6, monthDateBean.monthStr)
                        }
                    }

                    //生成某一天日期实体 日item
                    val dayDateBean =
                        DateBean()
                    dayDateBean.date = monthCalendar.time
                    dayDateBean.monthStr = monthDateBean.monthStr
                    dayDateBean.day =
                        monthCalendar[Calendar.DAY_OF_MONTH].toString() + ""
                    dateBeanList.add(dayDateBean)

                    //处理一个月的最后一天
                    if (monthCalendar.timeInMillis == endMonthDay.time) {
                        //看某个月最后一天是周几
                        val weekDay = monthCalendar[Calendar.DAY_OF_WEEK]
                        when (weekDay) {
                            1 -> addDatePlaceholder(dateBeanList, 6, monthDateBean.monthStr)
                            2 -> addDatePlaceholder(dateBeanList, 5, monthDateBean.monthStr)
                            3 -> addDatePlaceholder(dateBeanList, 4, monthDateBean.monthStr)
                            4 -> addDatePlaceholder(dateBeanList, 3, monthDateBean.monthStr)
                            5 -> addDatePlaceholder(dateBeanList, 2, monthDateBean.monthStr)
                            6 -> addDatePlaceholder(dateBeanList, 1, monthDateBean.monthStr)
                            7 -> {
                            }
                        }
                    }
                    //天数加1
                    monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
//                Timber.i(
//                    "日期：" + format.format(calendar.time) + "----周" + getWeekStr(
//                        calendar[Calendar.DAY_OF_WEEK].toString() + ""
//                    )
//                )
                //月份加1
                calendar.add(Calendar.MONTH, 1)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return dateBeanList
    }

    //添加空的日期占位
    private fun addDatePlaceholder(
        dateBeans: MutableList<DateBean>,
        count: Int,
        monthStr: String?
    ) {
        for (i in 0 until count) {
            val dateBean =
                DateBean()
            dateBean.monthStr = monthStr
            dateBeans.add(dateBean)
        }
    }

    //获取星期几
    private fun getWeekStr(mWay: String): String {
        var mWay = mWay
        if ("1" == mWay) {
            mWay = context.getString(R.string.sun)
        } else if ("2" == mWay) {
            mWay = context.getString(R.string.one)
        } else if ("3" == mWay) {
            mWay = context.getString(R.string.two)
        } else if ("4" == mWay) {
            mWay = context.getString(R.string.three)
        } else if ("5" == mWay) {
            mWay = context.getString(R.string.four)
        } else if ("6" == mWay) {
            mWay = context.getString(R.string.five)
        } else if ("7" == mWay) {
            mWay = context.getString(R.string.six)
        }
        return mWay
    }

    fun setSelectDate(selectStartTime: Long, selectEndTime: Long) {
        if (localDayList.isEmpty()) return

        selectStartDate = null
        selectEndDate = null
        var localIndex: Int? = null
        for ((index, item) in localDayList.withIndex()) {
            if (item.itemType == DateBean.ITEM_TYPE_MONTH) continue
            item.date?.let {
                if (it.time in selectStartTime..selectEndTime) {
                    if (selectStartDate == null) {
                        localIndex = index
                        selectStartDate = item
                    }
                    selectEndDate = item
                    item.itemState =
                        DateBean.ITEM_STATE_SELECTED
                }
            }
        }
        selectStartDate?.itemState =
            DateBean.ITEM_STATE_BEGIN_DATE
        selectEndDate?.itemState =
            DateBean.ITEM_STATE_END_DATE
        adapter?.notifyDataSetChanged()
        localIndex?.let {
            recyclerView.scrollToPosition(if (it > 10) (it - 10) else it)
        }
    }

    fun setMinMaxDate(minDateString: String, maxDateString: String) {
        this.minDate = minDateString
        this.maxDate = maxDateString
        generateUI()
    }

    private fun generateUI() {
        localDayList.clear()
        localDayList.addAll(days(minDate!!, maxDate!!))
        adapter?.notifyDataSetChanged()

        if (localDayList.isNotEmpty()) {
            recyclerView.scrollToPosition(localDayList.size - 1)
        }
    }

    fun isTypeBySingle(): Boolean = type == TYPE_SINGLE

    fun setTypeBySingle() {
        type =
            TYPE_SINGLE
    }

    interface OnDateSelected {
        //将选中的结果回传到Activity页面
        fun selected(
            startDate: String,
            endDate: String
        )

        //这个是在Dialog显示的情况下会用到，来判断如期是否已选完，来改变Dialog里面确定按钮的选中状态
        fun hasSelect(select: Boolean)
    }
}
