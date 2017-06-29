package com.raybritton.uiinspectorserver.ui.views

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.STROKE
import android.graphics.Typeface.NORMAL
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.raybritton.uiinspectorserver.data.model.TreeNode
import com.raybritton.uiinspectorserver.ui.main.TreeNodeRenderer
import timber.log.Timber
import java.util.*


class ScalpelLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0, @StyleRes defStyleRes: Int = 0) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), TreeNodeRenderer {

    interface OnClickListener {
        fun onClick(x: Int, y: Int)
    }

    private val TRACKING_UNKNOWN = 0
    private val TRACKING_VERTICALLY = 1
    private val TRACKING_HORIZONTALLY = -1
    private val ROTATION_MAX = 60
    private val ROTATION_MIN = -ROTATION_MAX
    private val ROTATION_DEFAULT_X = -10
    private val ROTATION_DEFAULT_Y = 15
    private val ZOOM_DEFAULT = 0.4f
    private val ZOOM_MIN = 0.33f
    private val ZOOM_MAX = 2f
    private val SPACING_DEFAULT = 25
    private val SPACING_MIN = 10
    private val SPACING_MAX = 100
    private val CHROME_COLOR = 0xFF888888.toInt()
    private val CHROME_SHADOW_COLOR = 0xFF000000.toInt()
    private val TEXT_OFFSET_DP = 2
    private val TEXT_SIZE_DP = 10
    private val CHILD_COUNT_ESTIMATION = 25
    private val OFFSET_MIN = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500f, context.resources.displayMetrics)
    private val OFFSET_MAX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600f, context.resources.displayMetrics)

    private class LayeredView {
        internal var view: View? = null
        internal var layer: Int = 0

        internal operator fun set(view: View, layer: Int) {
            this.view = view
            this.layer = layer
        }

        internal fun clear() {
            view = null
            layer = -1
        }
    }

    private val paddingPaint = Paint(ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = Color.argb(150, 150, 210, 230)
    }
    private val marginPaint = Paint(ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = Color.argb(150, 255, 193, 193)
    }
    private val highlightPaint = Paint(ANTI_ALIAS_FLAG).also {
        it.color = Color.RED
        it.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics)
        it.style = STROKE
    }
    private val viewBoundsRect = Rect()
    private val viewBorderPaint = Paint(ANTI_ALIAS_FLAG)
    private val camera = Camera()
    private val cameraMatrix = Matrix()
    private val location = IntArray(2)
    private val visibilities = BitSet(CHILD_COUNT_ESTIMATION)
    var listener: OnClickListener? = null
    private val layeredViewQueue = ArrayDeque<LayeredView>()
    private val layeredViewPool = object : Pool<LayeredView>(CHILD_COUNT_ESTIMATION) {
        override fun newObject(): LayeredView {
            return LayeredView()
        }
    }

    private var nodeViewMap = mapOf<String, TreeNode>()

    private var highlighted: TreeNode? = null
    private val density = getContext().resources.displayMetrics.density
    private val slop = ViewConfiguration.get(getContext()).scaledTouchSlop.toFloat()
    private val textOffset: Float
    private val textSize: Float

    //Always on for this app
    private var _3dEnabled: Boolean = false
    private var drawViews = true

    //Optional
    private var displayPadding = true
    private var displayMargin = true
    private var displayBorders = true
    private var overrideVisibility = true

    private var pointerOne = INVALID_POINTER_ID
    private var lastOneX: Float = 0.toFloat()
    private var lastOneY: Float = 0.toFloat()
    private var firstOneX: Float = 0.toFloat()
    private var firstOneY: Float = 0.toFloat()
    private var pointerTwo = INVALID_POINTER_ID
    private var lastTwoX: Float = 0.toFloat()
    private var lastTwoY: Float = 0.toFloat()
    private var multiTouchTracking = TRACKING_UNKNOWN

    private var uiRotationY = ROTATION_DEFAULT_Y.toFloat()
    private var uiRotationX = ROTATION_DEFAULT_X.toFloat()
    private var zoom = ZOOM_DEFAULT
    private var spacing = SPACING_DEFAULT.toFloat()
    private var uiYOffset = 0f

    init {

        textSize = TEXT_SIZE_DP * density
        textOffset = TEXT_OFFSET_DP * density

        viewBorderPaint.color = CHROME_COLOR
        viewBorderPaint.style = STROKE
        viewBorderPaint.textSize = textSize
        viewBorderPaint.setShadowLayer(1f, -1f, 1f, CHROME_SHADOW_COLOR)
        viewBorderPaint.typeface = Typeface.create("sans-serif-condensed", NORMAL)

        set3dEnabled(true)
    }

    fun set3dEnabled(enabled: Boolean) {
        if (this._3dEnabled != enabled) {
            this._3dEnabled = enabled
            setWillNotDraw(!enabled)
            invalidate()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return _3dEnabled || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!_3dEnabled) {
            return super.onTouchEvent(event)
        }

        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = if (action == ACTION_DOWN) 0 else event.actionIndex
                if (pointerOne == INVALID_POINTER_ID) {
                    pointerOne = event.getPointerId(index)
                    lastOneX = event.getX(index)
                    lastOneY = event.getY(index)
                    firstOneX = event.getX(index)
                    firstOneY = event.getY(index)
                    Timber.d("Got pointer 1!  id: %s  x: %s  y: %s", pointerOne, lastOneY, lastOneY)
                } else if (pointerTwo == INVALID_POINTER_ID) {
                    pointerTwo = event.getPointerId(index)
                    lastTwoX = event.getX(index)
                    lastTwoY = event.getY(index)
                    Timber.d("Got pointer 2!  id: %s  x: %s  y: %s", pointerTwo, lastTwoY, lastTwoY)
                } else {
                    Timber.d("Ignoring additional pointer.  id: %s", event.getPointerId(index))
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (pointerTwo == INVALID_POINTER_ID) {
                    // Single pointer controlling 3D rotation.
                    var i = 0
                    val count = event.pointerCount
                    while (i < count) {
                        if (pointerOne == event.getPointerId(i)) {
                            val eventX = event.getX(i)
                            val eventY = event.getY(i)
                            val dx = eventX - lastOneX
                            val dy = eventY - lastOneY
                            val drx = 90 * (dx / width)
                            val dry = 90 * (-dy / height) // Invert Y-axis.
                            // An 'x' delta affects 'y' rotation and vise versa.
                            uiRotationY = Math.min(Math.max(uiRotationY + drx, ROTATION_MIN.toFloat()), ROTATION_MAX.toFloat())
                            uiRotationX = Math.min(Math.max(uiRotationX + dry, ROTATION_MIN.toFloat()), ROTATION_MAX.toFloat())
                            Timber.d("Single pointer moved (%s, %s) affecting rotation (%s, %s).", dx, dy, drx, dry)

                            lastOneX = eventX
                            lastOneY = eventY

                            invalidate()
                        }
                        i++
                    }
                } else {
                    // We know there's two pointers and we only care about pointerOne and pointerTwo
                    val pointerOneIndex = event.findPointerIndex(pointerOne)
                    val pointerTwoIndex = event.findPointerIndex(pointerTwo)

                    val xOne = event.getX(pointerOneIndex)
                    val yOne = event.getY(pointerOneIndex)
                    val xTwo = event.getX(pointerTwoIndex)
                    val yTwo = event.getY(pointerTwoIndex)

                    val dxOne = xOne - lastOneX
                    val dyOne = yOne - lastOneY
                    val dxTwo = xTwo - lastTwoX
                    val dyTwo = yTwo - lastTwoY

                    if (multiTouchTracking == TRACKING_UNKNOWN) {
                        val adx = Math.abs(dxOne) + Math.abs(dxTwo)
                        val ady = Math.abs(dyOne) + Math.abs(dyTwo)

                        if (adx > slop * 2 || ady > slop * 2) {
                            if (adx > ady) {
                                // Left/right movement wins. Track horizontal.
                                multiTouchTracking = TRACKING_HORIZONTALLY
                            } else {
                                // Up/down movement wins. Track vertical.
                                multiTouchTracking = TRACKING_VERTICALLY
                            }
                        }
                    }

                    if (multiTouchTracking == TRACKING_VERTICALLY) {
                        if (yOne >= yTwo) {
                            zoom += dyOne / height - dyTwo / height
                        } else {
                            zoom += dyTwo / height - dyOne / height
                        }

                        zoom = Math.min(Math.max(zoom, ZOOM_MIN), ZOOM_MAX)
                        invalidate()
                    } else if (multiTouchTracking == TRACKING_HORIZONTALLY) {
                        if (xOne >= xTwo) {
                            spacing += dxOne / width * SPACING_MAX - dxTwo / width * SPACING_MAX
                        } else {
                            spacing += dxTwo / width * SPACING_MAX - dxOne / width * SPACING_MAX
                        }

                        spacing = Math.min(Math.max(spacing, SPACING_MIN.toFloat()), SPACING_MAX.toFloat())
                        invalidate()
                    }

                    if (multiTouchTracking != TRACKING_UNKNOWN) {
                        lastOneX = xOne
                        lastOneY = yOne
                        lastTwoX = xTwo
                        lastTwoY = yTwo
                    }
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val index = if (action != ACTION_POINTER_UP) 0 else event.actionIndex
                val pointerId = event.getPointerId(index)
                if (pointerOne == pointerId && pointerTwo == INVALID_POINTER_ID) {
                    listener?.onClick(event.x.toInt(), event.y.toInt())
                    pointerOne = INVALID_POINTER_ID
                    Timber.d("Finger removed without a second one touching")
                } else if (pointerOne == pointerId) {
                    // Shift pointer two (real or invalid) up to pointer one.
                    pointerOne = pointerTwo
                    lastOneX = lastTwoX
                    lastOneY = lastTwoY
                    Timber.d("Promoting pointer 2 (%s) to pointer 1.", pointerTwo)
                    // Clear pointer two and tracking.
                    pointerTwo = INVALID_POINTER_ID
                    multiTouchTracking = TRACKING_UNKNOWN
                } else if (pointerTwo == pointerId) {
                    Timber.d("Lost pointer 2 (%s).", pointerTwo)
                    pointerTwo = INVALID_POINTER_ID
                    multiTouchTracking = TRACKING_UNKNOWN
                }
            }
        }

        return true
    }

    override fun draw(canvas: Canvas) {
        if (!_3dEnabled) {
            super.draw(canvas)
            return
        }

        getLocationInWindow(location)
        val x = location[0].toFloat()
        val y = location[1].toFloat()

        val saveCount = canvas.save()

        val cx = width * 0.4f
        val cy = height * 0.5f

        camera.save()
        camera.rotate(uiRotationX, uiRotationY, 0f)
        camera.getMatrix(cameraMatrix)
        camera.restore()

        cameraMatrix.preTranslate(-cx, -cy)
        cameraMatrix.postTranslate(cx, cy)
        canvas.concat(cameraMatrix)
        canvas.scale(zoom, zoom, cx, cy)
        canvas.translate(0f, uiYOffset)

        if (!layeredViewQueue.isEmpty()) {
            throw AssertionError("View queue is not empty.")
        }

        // We don't want to be rendered so seed the queue with our children.
        (0 until childCount).forEach { i ->
            val layeredView = layeredViewPool.obtain()
            layeredView[getChildAt(i)] = 0
            layeredViewQueue.add(layeredView)
        }

        while (!layeredViewQueue.isEmpty()) {
            val layeredView = layeredViewQueue.removeFirst()
            val view = layeredView.view
            val layer = layeredView.layer

            // Restore the object to the pool for use later.
            layeredView.clear()
            layeredViewPool.restore(layeredView)

            // Hide any visible children.
            if (view is ViewGroup) {
                val viewGroup = view
                visibilities.clear()
                var i = 0
                val count = viewGroup.childCount
                while (i < count) {
                    val child = viewGroup.getChildAt(i)
                    val node = nodeViewMap[child.tag]
                    if (node != null) {
                        if (node.view.isVisible || overrideVisibility) {
                            visibilities.set(i)
                        }
                        child.visibility = View.INVISIBLE
                    } else {
                        if (child.visibility == View.VISIBLE) {
                            visibilities.set(i)
                            child.visibility = View.INVISIBLE
                        }
                    }
                    i++
                }
            }

            val viewSaveCount = canvas.save()

            // Scale the layer index translation by the rotation amount.
            val translateShowX = uiRotationY / ROTATION_MAX
            val translateShowY = uiRotationX / ROTATION_MAX
            val tx = layer.toFloat() * spacing * density * translateShowX
            val ty = layer.toFloat() * spacing * density * translateShowY
            canvas.translate(tx, -ty)

            view!!.getLocationInWindow(location)
            canvas.translate(location[0] - x, location[1] - y)

            if (displayBorders) {
                viewBoundsRect.set(0, 0, view.width, view.height)
                canvas.drawRect(viewBoundsRect, viewBorderPaint)
            }

            if (drawViews) {
                view.draw(canvas)
            }

            val node = nodeViewMap[view.tag]

            if (node != null) {
                val width = node.view.w.toFloat()
                val height = node.view.h.toFloat()

                if (view is ImageView) {
                    view.imageAlpha = (node.view.alpha * 255).toInt()
                } else {
                    view.alpha = node.view.alpha
                }

                if (displayPadding) {
                    canvas.drawRect(0f, 0f, node.view.leftPadding, height, paddingPaint);
                    canvas.drawRect(0f, 0f, width, node.view.topPadding, paddingPaint);
                    canvas.drawRect(width - node.view.rightPadding, 0f, width, height, paddingPaint);
                    canvas.drawRect(0f, height - node.view.bottomPadding, width, height, paddingPaint);
                }

                if (displayMargin) {
                    canvas.drawRect(-node.view.leftMargin, 0f, 0f, height, marginPaint);
                    canvas.drawRect(0f, -node.view.topMargin, width, 0f, marginPaint);
                    canvas.drawRect(width, 0f, width + node.view.rightMargin, height, marginPaint);
                    canvas.drawRect(0f, height, width, height + node.view.bottomMargin, marginPaint);
                }

                if (node == highlighted) {
                    canvas.drawRect(0f, 0f, view.width.toFloat(), view.height.toFloat(), highlightPaint)
                }
            }

            canvas.restoreToCount(viewSaveCount)

            // Restore any hidden children and queue them for later drawing.
            if (view is ViewGroup) {
                val viewGroup = view
                var i = 0
                val count = viewGroup.childCount
                while (i < count) {
                    if (visibilities.get(i)) {
                        val child = viewGroup.getChildAt(i)

                        child.visibility = View.VISIBLE
                        val childLayeredView = layeredViewPool.obtain()
                        childLayeredView[child] = layer + 1
                        layeredViewQueue.add(childLayeredView)
                    }
                    i++
                }
            }
        }

        canvas.restoreToCount(saveCount)
    }

    override fun setNodeViewMap(map: Map<String, TreeNode>) {
        nodeViewMap = map
    }

    private abstract class Pool<T> internal constructor(initialSize: Int) {
        private val pool: Deque<T>

        init {
            pool = ArrayDeque<T>(initialSize)
            for (i in 0..initialSize - 1) {
                pool.addLast(newObject())
            }
        }

        internal fun obtain(): T {
            return if (pool.isEmpty()) newObject() else pool.removeLast()
        }

        internal fun restore(instance: T) {
            pool.addLast(instance)
        }

        protected abstract fun newObject(): T
    }

    fun setShowPadding(value: Boolean) {
        if (displayPadding != value) {
            displayPadding = value
            invalidate()
        }
    }

    fun setShowMargin(value: Boolean) {
        if (displayMargin != value) {
            displayMargin = value
            invalidate()
        }
    }

    fun setShowBorders(value: Boolean) {
        if (displayBorders != value) {
            displayBorders = value
            invalidate()
        }
    }

    fun setHighlight(node: TreeNode) {
        if (highlighted != node) {
            highlighted = node
            invalidate()
        }
    }

    fun setShowInvisibleViews(value: Boolean) {
        if (overrideVisibility != value) {
            overrideVisibility = value
            invalidate()
        }
    }

    fun reset() {
        uiRotationX = 0f
        uiRotationY = 0f
        spacing = 0f
        invalidate()
    }

    fun setFocusPoint(value: Float) {
        uiYOffset = (OFFSET_MAX * value) - OFFSET_MIN
        invalidate()
    }
}
