package com.banketree.daterangepicker.core

import java.util.*

internal class DateBean {
    companion object {
        //item类型
        const val ITEM_TYPE_DAY = 1 //日期item
        const val ITEM_TYPE_MONTH = 2 //月份item

        //item状态
        const val ITEM_STATE_BEGIN_DATE = 1 //开始日期
        const val ITEM_STATE_END_DATE = 2 //结束日期
        const val ITEM_STATE_SELECTED = 3 //选中状态
        const val ITEM_STATE_NORMAL = 4 //正常状态
    }

    var itemType: Int = 1 //默认是日期item
    var itemState: Int =
        ITEM_STATE_NORMAL

    //具体日期
    var date: Date? = null

    //一个月的某天
    var day: String? = null

    //月份
    var monthStr: String? = null
}
