package com.kaii.trainspotter.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation
import com.kaii.trainspotter.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun createTrainIcon(
    context: Context,
    bearing: Int?,
    color: Int
): Bitmap {
    val trainDrawable = ContextCompat.getDrawable(context, R.drawable.train_filled_48px)!!
    trainDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

    val trainWidth = trainDrawable.intrinsicWidth
    val trainHeight = trainDrawable.intrinsicHeight

    val bitmap = createBitmap(trainWidth * 2, trainHeight * 2)
    val canvas = Canvas(bitmap)

    val centerX = canvas.width / 2
    val centerY = canvas.height / 2

    val left = centerX - trainWidth / 2
    val top = centerY - trainHeight / 2

    trainDrawable.setBounds(left, top, left + trainWidth, top + trainHeight)
    trainDrawable.draw(canvas)

    if (bearing != null) {
        val bearing = bearing - 90

        val arrowDrawable = ContextCompat.getDrawable(context, R.drawable.arrow_right)!!
        arrowDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

        val arrowWidth = arrowDrawable.intrinsicWidth * 1.25f
        val arrowHeight = arrowDrawable.intrinsicHeight * 1.25f

        val radius = centerX - (arrowWidth * 0.6f)
        val radians = bearing * PI / 180f

        val arrowX = centerX + radius * cos(radians)
        val arrowY = centerY + radius * sin(radians)

        arrowDrawable.setBounds(
            (arrowX - arrowWidth / 2f).toInt(),
            (arrowY - arrowHeight / 2f).toInt(),
            (arrowX + arrowWidth / 2f).toInt(),
            (arrowY + arrowHeight / 2f).toInt()
        )

        canvas.withRotation(
            degrees = bearing.toFloat(),
            pivotX = arrowX.toFloat(),
            pivotY = arrowY.toFloat()
        ) {
            arrowDrawable.draw(this)
        }
    }

    return bitmap
}
