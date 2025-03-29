package com.example.yadshniya.Model

import android.content.Context
import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.example.yadshniya.CloudinaryCallback
import com.example.yadshniya.MyApplication
import java.io.File
import java.io.FileOutputStream

class CloudinaryModel {

    init{
        val config =  mapOf(
            "cloud_name" to "dtjbx70tu",
            "api_key" to "434255865877326",
            "api_secret" to "_sUIsZykcugIlLG2P9vG1u6VTyE"
        )

        MyApplication.context?.let {
            MediaManager.init(it, config)
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.defaultPolicy()
        }

    }

    fun uploadBitmap(bitmap: Bitmap, onComplete: CloudinaryCallback)
    {
        val context = MyApplication.context!! ?: return
        val filepath: File = bitmapToFile(bitmap, context)
        MediaManager.get().upload(filepath.path)
            .option("folder", "images")
            .callback(object: UploadCallback{
                override fun onStart(requestId: String?) {
//                    TODO("Not yet implemented")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
//                    TODO("Not yet implemented")
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<*, *>) {
                    val publicUrl = resultData["secure_url"] as? String ?: ""
                    onComplete(publicUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onComplete(null)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
//                    TODO("Not yet implemented")
                }
            }).dispatch()
    }

    private fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        var file = File(context.cacheDir, "temp_img_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        }
        return file
    }
}