package com.kaii.trainspotter.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

fun tintDrawable(
    context: Context,
    @DrawableRes drawableId: Int,
    color: Int
): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableId)!!

    drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

    val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}