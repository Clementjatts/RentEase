package com.example.rentease.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.rentease.data.api.RentEaseApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.Result

/**
 * Data class representing a property image item for UI
 */
data class PropertyImageItem(
    val uri: Uri,
    val isExisting: Boolean = false
)

class ImageUploader(
    private val context: Context,
    private val api: RentEaseApi
) {
    private val tag = "ImageUploader"

    suspend fun uploadImage(propertyId: Int, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Convert Uri to File
            val file = createTempImageFile(uri)

            // Create MultipartBody.Part for image
            val requestFile = file.asRequestBody(getMimeType(uri).toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // Create RequestBody for property_id
            val propertyIdPart = propertyId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            // Upload image
            val response = api.uploadPropertyImage(propertyIdPart, imagePart)

            // Delete temp file
            file.delete()

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    val data = apiResponse.data as? Map<*, *>
                    val imageUrl = data?.get("url") as? String

                    if (imageUrl != null) {
                        return@withContext Result.success(imageUrl)
                    } else {
                        Log.e(tag, "Upload response missing URL: $data")
                        return@withContext Result.failure(Exception("Upload response missing URL"))
                    }
                } else {
                    val errorMessage = apiResponse?.message ?: "Unknown error"
                    Log.e(tag, "Upload failed: $errorMessage")
                    return@withContext Result.failure(Exception("Failed to upload image: $errorMessage"))
                }
            } else {
                Log.e(tag, "Upload request failed: ${response.code()} - ${response.message()}")
                return@withContext Result.failure(Exception("Failed to upload image: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception during upload", e)
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
}
