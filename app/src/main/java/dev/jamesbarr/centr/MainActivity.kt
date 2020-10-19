package dev.jamesbarr.centr

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRect
import androidx.lifecycle.lifecycleScope
import androidx.ui.tooling.preview.Preview
import dev.chrisbanes.insetter.Insetter
import dev.jamesbarr.centr.ui.CentrTheme
import kotlinx.coroutines.launch

private const val SHOW_DEBUG_STATS = false

class MainActivity : AppCompatActivity() {

  private val image = mutableStateOf<Drawable?>(null)
  private val imageLoader by lazy { ImageLoader(this) }
  private val wallpaperManager by lazy { getSystemService<WallpaperManager>() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.apply {
      Insetter.setEdgeToEdgeSystemUiFlags(decorView, true)
      statusBarColor = android.graphics.Color.TRANSPARENT
      navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    setContent {
      CentrTheme {
        Surface(color = MaterialTheme.colors.background) {
          Wallpaper(image, onSetWallpaperClick = { image.value?.let { setCenteredWallpaper(it) } })
        }
      }
    }

    lifecycleScope.launch {
      imageLoader.loadImage(intent)?.let {
        image.value = it
      }
    }
  }

  private fun setCenteredWallpaper(drawable: Drawable) {
    Toast.makeText(this, "Centering wallpaper...", LENGTH_SHORT).show()
    val visibleRectOfImage = visibleRectOfWallpaper(
      image = RectF().set(drawable.intrinsicWidth, drawable.intrinsicHeight),
      display = displaySize(this@MainActivity)
    ).toRect()
    wallpaperManager?.setBitmap(drawable.toBitmap(), visibleRectOfImage, true)
    Toast.makeText(this, "Wallpaper centered!", Toast.LENGTH_SHORT).show()
  }
}

@Composable
fun Wallpaper(image: MutableState<Drawable?>, onSetWallpaperClick: () -> Unit = {}) {
  Box(modifier = Modifier.fillMaxSize(), alignment = Center) {
    image.value?.let { drawable ->
      val imageSize = RectF().set(drawable.intrinsicWidth, drawable.intrinsicHeight)
      val displaySize = displaySize(ContextAmbient.current)
      val resizedImageSize = resizedToCoverDisplaySize(imageSize, displaySize).toRect()
      val bitmap = drawable.toBitmap(resizedImageSize.width(), resizedImageSize.height())

      Stack(modifier = Modifier.fillMaxSize()) {
        Image(asset = bitmap.asImageAsset(), contentScale = ContentScale.None)
        Column(modifier = Modifier.align(Center), horizontalAlignment = CenterHorizontally) {
          if (SHOW_DEBUG_STATS) {
            Text(
              text = debugStatsText(bitmap, ContextAmbient.current),
              modifier = Modifier.padding(bottom = 8.dp)
            )
          }
          Button(onClick = onSetWallpaperClick) {
            Text(text = "Center Wallpaper")
          }
        }
      }
    } ?: NoSharedImage()
  }
}

@Composable
fun NoSharedImage() {
  Column(horizontalAlignment = CenterHorizontally) {
    Stack(modifier = Modifier.clip(CircleShape).drawShadow(16.dp, CircleShape)) {
      listOf(R.drawable.ic_launcher_background, R.drawable.ic_launcher_foreground).forEach { resId ->
        Image(asset = vectorResource(id = resId), modifier = Modifier.size(128.dp))
      }
    }
    Text(text = "Share an Image or Uri with Centr to begin", modifier = Modifier.padding(all = 16.dp))
  }
}

@Preview(widthDp = 300, heightDp = 600)
@Composable
fun FatterImageThanScreenPreview() {
  CentrTheme {
    with(DensityAmbient.current) {
      Wallpaper(mutableStateOf(previewImage(Dp(800f).toIntPx(), Dp(400f).toIntPx())))
    }
  }
}

@Preview(widthDp = 300, heightDp = 600)
@Composable
fun SkinnierImageThanScreenPreview() {
  CentrTheme {
    with(DensityAmbient.current) {
      Wallpaper(mutableStateOf(previewImage(Dp(400f).toIntPx(), Dp(800f).toIntPx())))
    }
  }
}

@Preview
@Composable
fun NoImagePreview() {
  NoSharedImage()
}

private fun previewImage(@Px width: Int, @Px height: Int): Drawable {
  return BitmapDrawable(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
    Canvas(this).apply {
      drawColor(android.graphics.Color.BLUE)
    }
  })
}
