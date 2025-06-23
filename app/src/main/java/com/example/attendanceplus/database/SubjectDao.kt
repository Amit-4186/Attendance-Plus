package com.example.attendanceplus.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject)

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE subjects SET present = present + 1 WHERE id = :subjectId")
    suspend fun incrementPresent(subjectId: Long)

    @Query("UPDATE subjects SET absent = absent + 1 WHERE id = :subjectId")
    suspend fun incrementAbsent(subjectId: Long)

    @Query("UPDATE subjects SET present = present - 1 WHERE id = :subjectId AND present > 0")
    suspend fun decrementPresent(subjectId: Long)

    @Query("UPDATE subjects SET absent = absent - 1 WHERE id = :subjectId AND absent > 0")
    suspend fun decrementAbsent(subjectId: Long)
}