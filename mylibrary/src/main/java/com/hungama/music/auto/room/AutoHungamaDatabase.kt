package com.hungama.music.auto.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hungama.music.auto.channel.Channel
import com.hungama.music.auto.room.dao.RecentPlayedChannelDao

@Database(
    entities = [Channel::class],
    version = 1,
    exportSchema = false
)
abstract class AutoHungamaDatabase : RoomDatabase() {

    companion object {
        private var INSTANCE: AutoHungamaDatabase? = null

        fun getInstance(context: Context): AutoHungamaDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AutoHungamaDatabase::class.java,
                    "hungama.db"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return requireNotNull(INSTANCE)
        }

    }

    abstract fun recentPlayedDao(): RecentPlayedChannelDao
}
