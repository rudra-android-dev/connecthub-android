package com.example.connecthub.data.repository

import android.content.Context
import android.net.Uri
import com.example.connecthub.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class ImageRepository(private val context: Context) {

    private val apiService: ImgBbApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgBbApiService::class.java)
    }

    suspend fun uploadProfileImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, uri) ?: return@withContext null
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val apiKey = context.getString(R.string.imgbb_api_key)

            val response = apiService.uploadImage(apiKey, body)
            if (response.isSuccessful && response.body()?.success == true) {
                return@withContext response.body()?.data?.url
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val file = File(context.cacheDir, "temp_profile_upload.jpg")
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}