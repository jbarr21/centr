package dev.jamesbarr.centr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.RectF
import androidx.core.graphics.toRect

fun resizedToCoverDisplaySize(image: RectF, display: RectF): RectF {
  val imageToDisplayRatio = if (image.aspectRatio() > display.aspectRatio()) {
    // fit image to screen height
    image.height() / display.height()
  } else {
    // fit image to screen width
    image.width() / display.width()
  }
  return RectF().apply {
    right = image.width() / imageToDisplayRatio
    bottom = image.height() / imageToDisplayRatio
  }
}

fun visibleRectOfWallpaper(image: RectF, display: RectF): RectF {
  val imageViewport = RectF(image)
  if (image.aspectRatio() > display.aspectRatio()) {
    // show all of image height
    val imageToDisplayRatio = image.height() / display.height()
    display.scale(imageToDisplayRatio)
    imageViewport.apply {
      left = image.centerX() - display.width() / 2f
      right = image.centerX() + display.width() / 2f
    }
  } else {
    // show all of image width
    val imageToDisplayRatio = image.width() / display.width()
    display.scale(imageToDisplayRatio)
    imageViewport.apply {
      top = image.centerY() - display.height() / 2f
      bottom = image.centerY() + display.height() / 2f
    }
  }
  return imageViewport
}

fun displaySize(context: Context): RectF {
  val (x, y) = context.display?.let {
    val point = Point()
    it.getRealSize(point)
    point.x to point.y
  } ?: context.resources.displayMetrics.let { it.widthPixels to it.heightPixels }
  return RectF(0f, 0f, x.toFloat(), y.toFloat())
}

fun debugStatsText(bitmap: Bitmap, context: Context): String {
  val displaySize = displaySize(context)
  val imageSize = RectF().set(bitmap.width, bitmap.height)
  val resizedImageSize = resizedToCoverDisplaySize(imageSize, displaySize)
  val imageViewport = visibleRectOfWallpaper(imageSize, displaySize)

  return """
    DisplaySize = ${displaySize.dimens()}
          Image = ${imageSize.dimens()}
     ResizedImg = ${resizedImageSize.dimens()}
       Viewport = ${imageViewport.toRect()}
  """.trimIndent()
}

fun RectF.aspectRatio() = width() / height()
fun RectF.scale(multiple: Float) = set(0f, 0f, width() * multiple, height() * multiple)
fun RectF.set(width: Int, height: Int) = apply { set(0f, 0f, width.toFloat(), height.toFloat()) }
fun RectF.dimens() = "${width().toInt()} x ${height().toInt()} pixels"
