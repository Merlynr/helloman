package com.lifeonline.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lifeonline.app.data.local.dao.BuffDao
import com.lifeonline.app.data.local.dao.DailyLogDao
import com.lifeonline.app.data.local.dao.TaskDao
import com.lifeonline.app.data.local.dao.UserStatsDao
import com.lifeonline.app.data.model.BuffItem
import com.lifeonline.app.data.model.DailyLog
import com.lifeonline.app.data.model.TaskItem
import com.lifeonline.app.data.model.UserStats

@Database(
    entities = [
        UserStats::class,
        DailyLog::class,
        TaskItem::class,
        BuffItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userStatsDao(): UserStatsDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun taskDao(): TaskDao
    abstract fun buffDao(): BuffDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_online.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
