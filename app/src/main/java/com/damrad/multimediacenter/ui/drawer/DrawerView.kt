package com.damrad.multimediacenter.ui.drawer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs


class DrawerView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var BRUSH_SIZE = 20
    val DEFAULT_COLOR: Int = Color.RED
    private val DEFAULT_BG_COLOR: Int = Color.WHITE
    private val TOUCH_TOLERANCE = 4f
    private var myX = 0f
    private var myY = 0f
    private var myPath: Path? = null
    private var paint: Paint? = null
    private var paths: ArrayList<MyBrush> = ArrayList()
    private var currentColor = 0
    private var myBackgroundColor = DEFAULT_BG_COLOR
    private var strokeWidth = 0
    private var emboss = false
    private var blur = false
    private var myEmboss: MaskFilter? = null
    private var myBlur: MaskFilter? = null
    private var myBitmap: Bitmap? = null
    private var myCanvas: Canvas? = null
    private var myBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)

    init {
        paint = object : Paint() {}
        paint?.isAntiAlias = true
        paint?.isDither = true
        paint?.color = DEFAULT_COLOR
        paint?.style = Paint.Style.STROKE
        paint?.strokeJoin = Paint.Join.ROUND
        paint?.strokeCap = Paint.Cap.ROUND
        paint?.xfermode = null
        paint?.alpha = 0xff

        myEmboss = EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 0.4f, 6f, 3.5f)
        myBlur = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)
    }

    fun init(metrics: DisplayMetrics) {
        val height = metrics.heightPixels
        val width = metrics.widthPixels
        myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        myCanvas = Canvas(myBitmap!!)
        currentColor = DEFAULT_COLOR
        strokeWidth = BRUSH_SIZE
    }

    fun normal() {
        emboss = false
        blur = false
    }

    fun emboss() {
        emboss = true
        blur = false
    }

    fun blur() {
        emboss = false
        blur = true
    }

    fun clear() {
        myBackgroundColor = DEFAULT_BG_COLOR
        paths.clear()
        normal()
        invalidate()
    }

    public override fun onDraw(canvas: Canvas) {
        canvas.save()
        myCanvas?.drawColor(myBackgroundColor)
        for (myBrush in paths) {
            paint?.color = myBrush.color
            paint?.strokeWidth = myBrush.strokeWidth.toFloat()
            paint?.maskFilter = null
            if (myBrush.emboss) paint?.maskFilter = myEmboss else if (myBrush.blur) paint?.maskFilter = myBlur
            paint?.let { myCanvas?.drawPath(myBrush.path, it) }
        }
        myBitmap?.let { canvas.drawBitmap(it, 0f, 0f, myBitmapPaint) }
        canvas.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        myPath = Path()
        val fp = MyBrush(currentColor, emboss, blur, strokeWidth, myPath!!)
        paths.add(fp)
        myPath?.reset()
        myPath?.moveTo(x, y)
        myX = x
        myY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx: Float = abs(x - myX)
        val dy: Float = abs(y - myY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            myPath?.quadTo(myX, myY, (x + myX) / 2, (y + myY) / 2)
            myX = x
            myY = y
        }
    }

    private fun touchUp() {
        myPath?.lineTo(myX, myY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

}