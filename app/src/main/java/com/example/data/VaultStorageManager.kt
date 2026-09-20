package com.example.data

import android.app.RecoverableSecurityException
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class VaultStorageManager(private val context: Context) {

    private val vaultDir: File by lazy {
        val dir = File(context.filesDir, "vault_photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    private val cameraTempDir: File by lazy {
        val dir = File(context.cacheDir, "camera_temp")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    fun importPhotoFromUri(uri: Uri): VaultPhoto? {
        return try {
            val contentResolver = context.contentResolver
            var originalName = "photo_${System.currentTimeMillis()}.jpg"
            var size = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        originalName = cursor.getString(nameIndex) ?: originalName
                    }
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }

            val uniqueFileName = "vault_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(vaultDir, uniqueFileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val finalSize = if (targetFile.exists()) targetFile.length() else size

            VaultPhoto(
                filePath = targetFile.absolutePath,
                fileName = uniqueFileName,
                dateAdded = System.currentTimeMillis(),
                fileSizeBytes = finalSize,
                originalUri = uri.toString(),
                originalDeletedFromDevice = false,
                title = originalName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createVaultCameraFile(): Pair<File, Uri> {
        val uniqueFileName = "vault_cam_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val targetFile = File(vaultDir, uniqueFileName)
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, targetFile)
        return Pair(targetFile, contentUri)
    }

    fun createVaultPhotoFromFile(file: File): VaultPhoto {
        return VaultPhoto(
            filePath = file.absolutePath,
            fileName = file.name,
            dateAdded = System.currentTimeMillis(),
            fileSizeBytes = if (file.exists()) file.length() else 0L,
            originalUri = null,
            originalDeletedFromDevice = false,
            title = "Secret Capture"
        )
    }

    fun deletePhotoFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteOriginalFromPhone(
        uriString: String,
        deleteSenderLauncher: ActivityResultLauncher<IntentSenderRequest>? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val uri = Uri.parse(uriString)
            val contentResolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pendingIntent = MediaStore.createDeleteRequest(contentResolver, listOf(uri))
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                if (deleteSenderLauncher != null) {
                    deleteSenderLauncher.launch(intentSenderRequest)
                } else {
                    onError("System deletion launcher not available")
                }
            } else {
                val rows = contentResolver.delete(uri, null, null)
                if (rows > 0) {
                    onSuccess()
                } else {
                    onError("Could not delete file from device")
                }
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                if (deleteSenderLauncher != null) {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.userAction.actionIntent.intentSender).build()
                    deleteSenderLauncher.launch(intentSenderRequest)
                } else {
                    onError("Permission required to delete")
                }
            } else {
                onError(e.localizedMessage ?: "Deletion failed")
            }
        }
    }
}
