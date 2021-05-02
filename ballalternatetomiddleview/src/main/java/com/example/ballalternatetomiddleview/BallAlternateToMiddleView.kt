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
    "",
    "",
    "",
    "",
    ""
).map {
    Color.parseColor(it)
}.toTypedArray()
val circles : Int = 5
val parts : Int = 4
val scGap : Float = 0.02f / parts
val sizeFactor : Float = 8.5f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBallAlternateToMiddle(scale : Float, w : Float, h : Float, paint : Paint) {
    val r : Float = Math.min(w, h) / sizeFactor
    val gap : Float = (w - 2 * r) / circles
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
