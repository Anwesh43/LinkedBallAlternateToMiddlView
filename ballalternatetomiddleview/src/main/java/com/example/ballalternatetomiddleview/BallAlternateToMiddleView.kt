package com.example.ballalternatetomiddleview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF
import android.app.Activity
import android.content.Context

val colors : Array<Int> = arrayOf(
    "#f44336",
    "#3F51B5",
    "#FF9800",
    "#006064",
    "#311B92"
).map {
    Color.parseColor(it)
}.toTypedArray()
val circles : Int = 5
val parts : Int = 4
val scGap : Float = 0.02f / parts
val sizeFactor : Float = 12.5f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBallAlternateToMiddle(scale : Float, w : Float, h : Float, paint : Paint) {
    val r : Float = Math.min(w, h) / sizeFactor
    val gap : Float = (w - 2 * r) / (circles - 1)
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    save()
    translate(w / 2, h / 2)
    for (j in 0..(circles - 1)) {
        val x : Float = -w / 2 + r + gap * j
        save()
        translate(x * (1 - sf3), (h / 2 - r) * (j % 2) * (1 - sf2))
        drawArc(RectF(-r, -r, r, r), 0f, 360f * sf1, true, paint)
        restore()
    }
    restore()
}

fun Canvas.drawBATMNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    drawBallAlternateToMiddle(scale, w, h, paint)
}

class BallAlternateToMiddleView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + scGap * dir
                if (Math.abs(scale - prevScale) > 1) {
                    scale = prevScale + dir
                    dir = 0f
                    prevScale = scale
                    cb(prevScale)
                }
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BATMNode(var i : Int, val state : State = State()) {

        private var next : BATMNode? = null
        private var prev : BATMNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size -1) {
                next = BATMNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBATMNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BATMNode {
            var curr : BATMNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BallAlternateToMiddle(var i : Int) {

        private var curr : BATMNode = BATMNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BallAlternateToMiddleView) {

        private val batm : BallAlternateToMiddle = BallAlternateToMiddle(0)
        private val animator : Animator = Animator(view)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            batm.draw(canvas, paint)
            animator.animate {
                batm.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            batm.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BallAlternateToMiddleView {
            val view : BallAlternateToMiddleView = BallAlternateToMiddleView(activity)
            activity.setContentView(view)
            return view
        }
    }
}