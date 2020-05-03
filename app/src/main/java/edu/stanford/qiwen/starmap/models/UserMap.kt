package edu.stanford.qiwen.starmap.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable

@Entity
data class UserMap(val title: String,
                   @TypeConverters(Converters::class)
                   val places: List<Place>) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}