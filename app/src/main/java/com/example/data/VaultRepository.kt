package com.example.data

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class VaultRepository(
    private val dao: VaultPhotoDao,
    private val storageManager: VaultStorageManager
) {
    val allPhotos: Flow<List<VaultPhoto>> = dao.getAllPhotos()

    suspend fun importPhoto(uri: Uri): VaultPhoto? = withContext(Dispatchers.IO) {
        val photo = storageManager.importPhotoFromUri(uri)
        if (photo != null) {
            val id = dao.insertPhoto(photo)
            photo.copy(id = id)
        } else {
            null
        }
    }

    suspend fun saveCameraCapture(file: File): VaultPhoto = withContext(Dispatchers.IO) {
        val photo = storageManager.createVaultPhotoFromFile(file)
        val id = dao.insertPhoto(photo)
        photo.copy(id = id)
    }

    suspend fun deletePhoto(photo: VaultPhoto) = withContext(Dispatchers.IO) {
        storageManager.deletePhotoFile(photo.filePath)
        dao.deletePhoto(photo)
    }

    suspend fun deletePhotos(photos: List<VaultPhoto>) = withContext(Dispatchers.IO) {
        photos.forEach { photo ->
            storageManager.deletePhotoFile(photo.filePath)
        }
        dao.deletePhotosByIds(photos.map { it.id })
    }

    suspend fun markOriginalDeleted(photoId: Long) = withContext(Dispatchers.IO) {
        dao.markOriginalAsDeleted(photoId)
    }

    fun prepareCameraCapture(): Pair<File, Uri> {
        return storageManager.createVaultCameraFile()
    }

    fun getStorageManager(): VaultStorageManager = storageManager
}
