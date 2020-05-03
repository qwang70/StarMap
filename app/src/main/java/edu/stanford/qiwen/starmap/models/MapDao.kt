package edu.stanford.qiwen.starmap.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MapDao {
    @Insert
    fun insertMap(vararg map: UserMap)

    @Insert
    fun insertMapAll(maps: List<UserMap>)

    @Query("DELETE FROM UserMap WHERE title=:title")
    fun deleteMap(vararg title: String)

    @Query("SELECT * FROM UserMap")
    fun getAll(): List<UserMap>

}