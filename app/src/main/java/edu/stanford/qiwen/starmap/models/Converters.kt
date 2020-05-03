package edu.stanford.qiwen.starmap.models

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromPlaces(places: List<Place>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Place>>() {}.getType()
        return gson.toJson(places, type)
    }

    @TypeConverter
    fun toPlaces(placesString: String): List<Place> {
        val gson = Gson()
        val type = object : TypeToken<List<Place>>() {}.type
        return gson.fromJson(placesString, type)
    }
}