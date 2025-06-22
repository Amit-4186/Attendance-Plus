package com.example.attendanceplus.inject

import android.content.Context
import com.example.attendanceplus.Repository
import com.example.attendanceplus.database.AppDatabase
import com.example.attendanceplus.database.AttendanceDao
import com.example.attendanceplus.database.ScheduleDao
import com.example.attendanceplus.database.SubjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSubjectDao(database: AppDatabase): SubjectDao = database.subjectDao()

    @Provides
    @Singleton
    fun provideScheduleDao(database: AppDatabase): ScheduleDao = database.scheduleDao()

    @Provides
    @Singleton
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()

    @Provides
    @Singleton
    fun provideTimetableRepository(
        subjectDao: SubjectDao,
        scheduleDao: ScheduleDao,
        attendanceDao: AttendanceDao
    ): Repository {
        return Repository(subjectDao, scheduleDao, attendanceDao)
    }
}