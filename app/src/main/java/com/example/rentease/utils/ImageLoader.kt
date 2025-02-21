package com.example.rentease.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.rentease.R
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class ImageLoader private constructor(context: Context) {
    private val imageCache = ImageCache.getInstance(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val loadingJobs = mutableMapOf<ImageView, Job>()
    private val placeholderDrawable = ContextCompat.getDrawable(context, R.drawable.placeholder_image)

    fun loadImage(imageView: ImageView, url: String) {
        // Cancel any existing loading job for this ImageView
        loadingJobs[imageView]?.cancel()

        // Set placeholder immediately
        imageView.setImageDrawable(placeholderDrawable)

        // Start new loading job
        val job = coroutineScope.launch {
            try {
                val bitmap = loadImageFromCache(url) ?: loadImageFromNetwork(url)
                bitmap?.let {
                    imageView.setImageBitmap(it)
                }
            } catch (e: Exception) {
                // Keep placeholder on error
            } finally {
                loadingJobs.remove(imageView)
            }
        }

        loadingJobs[imageView] = job

        // Clear reference when ImageView is detached
        imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {}
            override fun onViewDetachedFromWindow(v: View?) {
                job.cancel()
                loadingJobs.remove(imageView)
                imageView.removeOnAttachStateChangeListener(this)
            }
        })
    }

    private suspend fun loadImageFromCache(url: String): Bitmap? {
        return imageCache.getBitmap(url)
    }

    private suspend fun loadImageFromNetwork(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            bitmap?.let {
                // Cache the loaded bitmap
                imageCache.putBitmap(url, it)
            }

            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun cancelAll() {
        loadingJobs.values.forEach { it.cancel() }
        loadingJobs.clear()
    }

    companion object {
        @Volatile
        private var INSTANCE: ImageLoader? = null

        fun getInstance(context: Context): ImageLoader {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageLoader(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
