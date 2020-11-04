package com.banketree.daterangepicker.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

internal class MyItemDecoration : ItemDecoration() {
    var paint = Paint()
    var colorPaint = Paint()
    var linePaint = Paint()

    init {
        paint.color = Color.parseColor("#ffffff")
        paint.style = Paint.Style.FILL
        colorPaint.color = Color.parseColor("#F08519")
        colorPaint.isAntiAlias = true
        linePaint.isAntiAlias = true
        linePaint.color = Color.parseColor("#dddddd")
    }


    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(c, parent, state)
        if (parent.childCount <= 0) {
            return
        }

        //头部的高度
        var height = 35
        val scale = parent.context.resources.displayMetrics.density
        height = (height * scale + 0.5f).toInt()

        //获取第一个可见的view，通过此view获取其对应的月份
        val adapter = parent.adapter as CalendarAdapter?
        val fistView = parent.getChildAt(0)
        val text =
            adapter!!.data[parent.getChildAdapterPosition(fistView)].monthStr
        var fistMonthStr: String? = ""
        var fistViewTop = 0
        //查找当前可见的itemView中第一个月份类型的item
        for (i in 0 until parent.childCount) {
            val v = parent.getChildAt(i)
            if (2 == parent.getChildViewHolder(v).itemViewType) {
                fistMonthStr = adapter.data[parent.getChildAdapterPosition(v)].monthStr
                fistViewTop = v.top
                break
            }
        }

        //计算偏移量
        var topOffset = 0
        if (fistMonthStr != text && fistViewTop < height) {
            //前提是第一个可见的月份item不是当前显示的月份和距离的顶部的距离小于头部的高度
            topOffset = height - fistViewTop
        }
        val t = 0 - topOffset

        //绘制头部区域
        c.drawRect(
            parent.left.toFloat(),
            t.toFloat(),
            parent.right.toFloat(),
            t + height.toFloat(),
            paint
        )
        colorPaint.textAlign = Paint.Align.CENTER
        colorPaint.textSize = 13 * scale + 0.5f
        //绘制头部月份文字
        c.drawText(text!!, parent.right / 2.toFloat(), (t + height) / 2.toFloat(), colorPaint)

        //绘制分割线
//        if(fistViewTop!=height) {
//            linePaint.setStrokeWidth(scale * 0.5f + 0.5f);
//            c.drawLine(parent.getLeft(), t + height, parent.getRight(), t + height, linePaint);
//        }
    }
}
