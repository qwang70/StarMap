package edu.stanford.qiwen.starmap.models

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [UserMap::class], version = 1)
@TypeConverters(Converters::class)
abstract class UserMapDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao
    companion object {
        const val NAME = "UserMapDatabase"
    }
}