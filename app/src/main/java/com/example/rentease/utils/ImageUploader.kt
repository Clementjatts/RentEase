package com.example.rentease.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.rentease.data.api.RentEaseApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageUploader(
    private val context: Context,
    private val api: RentEaseApi
) {
    suspend fun uploadImage(uri: Uri, propertyId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Convert Uri to File
            val file = createTempImageFile(uri)
            
            // Create MultipartBody.Part
            val requestFile = file.asRequestBody(getMimeType(uri).toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // Upload image
            val response = api.uploadPropertyImage(propertyId, body)
            
            // Delete temp file
            file.delete()

            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception("Failed to upload image: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createTempImageFile(uri: Uri): File = withContext(Dispatchers.IO) {
        // Create temporary file
        val tempFile = File(context.cacheDir, "img_${UUID.randomUUID()}.jpg")
        
        // Read and compress the image
        context.contentResolver.openInputStream(uri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            FileOutputStream(tempFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
            }
        }

        tempFile
    }

    private fun getMimeType(uri: Uri): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
    }

    companion object {
        private const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
    }
}
