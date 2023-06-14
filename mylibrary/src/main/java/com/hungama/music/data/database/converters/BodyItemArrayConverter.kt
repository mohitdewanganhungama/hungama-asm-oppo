package com.hungama.music.data.database.converters


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hungama.music.data.model.BodyDataItem

/**
 * Created by chetan patel
 */

public class BodyItemArrayConverter {
    @TypeConverter
    public fun fromString(value: String): BodyDataItem? {
        val listType = object : TypeToken<BodyDataItem>() {

        }.type
        return Gson().fromJson<BodyDataItem>(value, listType)
    }

    @TypeConverter
    public fun fromArrayList(list: BodyDataItem): String {
        return  Gson().toJson(list)
    }

}
