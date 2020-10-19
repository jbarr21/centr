package dev.jamesbarr.centr

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import coil.Coil
import coil.request.ImageRequest

class ImageLoader(private val context: Context) {

  fun imageUriExtra(intent: Intent): Uri? {
    return when {
      intent.action != ACTION_SEND -> null
      "text/plain" == intent.type -> Uri.parse(intent.getStringExtra(EXTRA_TEXT).orEmpty())
      intent.type?.startsWith("image/") == true -> intent.getParcelableExtra(EXTRA_STREAM)!!
      else -> null
    }
  }

  suspend fun loadImage(intent: Intent): Drawable? {
    return ImageRequest.Builder(context)
      .data(imageUriExtra(intent))
      .build()
      .let { Coil.execute(it).drawable }
  }
}
