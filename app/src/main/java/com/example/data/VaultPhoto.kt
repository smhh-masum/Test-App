package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_photos")
data class VaultPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0L,
    val originalUri: String? = null,
    val originalDeletedFromDevice: Boolean = false,
    val title: String = ""
)
