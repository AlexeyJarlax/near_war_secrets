//package com.pavlov.MyShadowGallery.util
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.util.AttributeSet
//import androidx.appcompat.widget.AppCompatImageView
//import androidx.recyclerview.widget.RecyclerView
//import com.pavlov.MyShadowGallery.PhotoListAdapter
//
//class ExclamationMarkImageView(context: Context, attrs: AttributeSet?, ) : AppCompatImageView(context, attrs) {
//    private val paint = Paint()
//
//    init {
//        paint.isAntiAlias = true
//        paint.color = Color.RED
//        paint.textSize = 120f
//        paint.style = Paint.Style.FILL_AND_STROKE
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        if (PhotoListAdapter.dodidon) {
//            drawExclamationMark(canvas, "!")
//        } else {
//            drawExclamationMark(canvas, "гуд")
//        }
//    }
//
//    private fun drawExclamationMark(canvas: Canvas, string: String) {
//        val x = (width - paint.measureText("!")) / 10
//        val y = (height - (paint.descent() + paint.ascent())) / 5
//        canvas.drawText(string, x, y, paint)
//    }
//}
