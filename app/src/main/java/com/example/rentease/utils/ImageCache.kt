package com.example.rentease.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class ImageCache private constructor(context: Context) {
    private val memoryCache: LruCache<String, Bitmap>
    private val diskCache: File
    private val maxDiskCacheSize = 100L * 1024 * 1024 // 100MB

    init {
        // Initialize memory cache (using 1/8th of available memory)
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }

        // Initialize disk cache
        diskCache = File(context.cacheDir, "image_cache")
        if (!diskCache.exists()) {
            diskCache.mkdirs()
        }

        // Clean up old cache files if size exceeds maxDiskCacheSize
        cleanupDiskCache()
    }

    suspend fun getBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        val key = hashKey(url)

        // Try memory cache first
        memoryCache.get(key)?.let { return@withContext it }

        // Try disk cache
        val file = File(diskCache, key)
        if (file.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.let { memoryCache.put(key, it) }
                return@withContext bitmap
            } catch (e: Exception) {
                file.delete()
            }
        }

        null
    }

    suspend fun putBitmap(url: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val key = hashKey(url)

        // Save to memory cache
        memoryCache.put(key, bitmap)

        // Save to disk cache
        val file = File(diskCache, key)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: Exception) {
            file.delete()
        }

        // Cleanup if necessary
        cleanupDiskCache()
    }

    private fun cleanupDiskCache() {
        val files = diskCache.listFiles() ?: return
        var totalSize = 0L
        val sortedFiles = files.sortedBy { it.lastModified() }

        for (file in sortedFiles) {
            totalSize += file.length()
        }

        // Remove oldest files until we're under maxDiskCacheSize
        var i = 0
        while (totalSize > maxDiskCacheSize && i < sortedFiles.size) {
            val file = sortedFiles[i]
            totalSize -= file.length()
            file.delete()
            i++
        }
    }

    private fun hashKey(key: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(key.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun clear() {
        memoryCache.evictAll()
        diskCache.listFiles()?.forEach { it.delete() }
    }

    companion object {
        @Volatile
        private var INSTANCE: ImageCache? = null

        fun getInstance(context: Context): ImageCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageCache(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
