package com.raybritton.uiinspectorserver.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.raybritton.uiinspectorserver.R

class VerticalSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : View(context, attrs, defStyleAttr, defStyleRes) {
    interface OnPositionChangeListener {
        fun onPositionChange(value: Float)
    }

    private val barPaint = Paint().also {
        it.color = Color.argb(150, 0, 0, 0)
    }
    private val thumbPaint = Paint().also {
        it.color = context.getColor(R.color.colorAccent)
    }
    private val halfBarWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics)
    private val thumbRect = Rect().apply {
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics)
        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics)
        set(0, 0, width.toInt(), height.toInt())
    }

    private var position = 0f;
    private var barStart = PointF()
    private var barEnd = PointF()

    private var touching = false

    var onPositionChangeListener: OnPositionChangeListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        barStart.x = (measuredWidth * 0.5f + paddingLeft - paddingRight) - halfBarWidth
        barStart.y = 0f + paddingTop
        barEnd.x = barStart.x + halfBarWidth + halfBarWidth
        barEnd.y = (measuredHeight - paddingBottom).toFloat()

        updateThumbPos()
    }

    private fun updateThumbPos() {
        val x = barStart.x - (thumbRect.width() * 0.5f)
        val y = ((barEnd.y - barStart.y) * position) + barStart.y

        thumbRect.offsetTo(x.toInt(), y.toInt())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(barStart.x, barStart.y, barEnd.x, barEnd.y, barPaint)
        canvas.drawRect(thumbRect, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                updatePosition(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (touching) {
                    updatePosition(event.x, event.y)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_OUTSIDE -> {
                if (touching) {
                    touching = false
                    updatePosition(event.x, event.y)
                }
            }
        }

        updateThumbPos()
        onPositionChangeListener?.onPositionChange(position)
        return true
    }

    private fun updatePosition(x: Float, y: Float) {
        position = Math.max(0f, Math.min(1f, y / (measuredHeight - paddingTop - paddingBottom)))
    }

    fun setPosition(value: Float) {
        position = value
        updateThumbPos()
        onPositionChangeListener?.onPositionChange(position)
    }
}
