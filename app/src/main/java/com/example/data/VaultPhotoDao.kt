package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultPhotoDao {
    @Query("SELECT * FROM vault_photos ORDER BY dateAdded DESC")
    fun getAllPhotos(): Flow<List<VaultPhoto>>

    @Query("SELECT * FROM vault_photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): VaultPhoto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: VaultPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: VaultPhoto)

    @Query("DELETE FROM vault_photos WHERE id IN (:ids)")
    suspend fun deletePhotosByIds(ids: List<Long>)

    @Query("UPDATE vault_photos SET originalDeletedFromDevice = 1 WHERE id = :id")
    suspend fun markOriginalAsDeleted(id: Long)
}
