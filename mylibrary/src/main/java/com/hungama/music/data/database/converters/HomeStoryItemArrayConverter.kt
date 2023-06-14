package com.hungama.music.data.database.converters


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hungama.music.data.model.HomeStory

/**
 * Created by chetan patel
 */

public class HomeStoryItemArrayConverter {
    @TypeConverter
    public fun fromString(value: String): List<HomeStory>? {
        val listType = object : TypeToken<List<HomeStory>>() {

        }.type
        return Gson().fromJson<List<HomeStory>>(value, listType)
    }

    @TypeConverter
    public fun fromArrayList(list: List<HomeStory>): String {
        return  Gson().toJson(list)
    }

}
