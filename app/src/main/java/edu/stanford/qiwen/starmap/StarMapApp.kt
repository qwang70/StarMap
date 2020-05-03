package edu.stanford.qiwen.starmap

import android.app.Application
import androidx.room.Room
import edu.stanford.qiwen.starmap.models.UserMapDatabase

class StarMapApp : Application() {

    companion object {
        var userMapDatabase: UserMapDatabase? = null
        fun getDatabase(): UserMapDatabase? {
            return userMapDatabase
        }

    }

    override fun onCreate() {
        super.onCreate()
        userMapDatabase = Room.databaseBuilder(this, UserMapDatabase::class.java,
                UserMapDatabase.NAME).fallbackToDestructiveMigration().build()
    }
}