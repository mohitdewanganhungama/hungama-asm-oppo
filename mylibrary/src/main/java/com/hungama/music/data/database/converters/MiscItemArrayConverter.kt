package com.hungama.music.data.database.converters


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hungama.music.data.model.Misc

/**
 * Created by chetan patel
 */

public class MiscItemArrayConverter {
    @TypeConverter
    public fun fromString(value: String): Misc? {
        val listType = object : TypeToken<Misc>() {

        }.type
        return Gson().fromJson<Misc>(value, listType)
    }

    @TypeConverter
    public fun fromArrayList(list: Misc): String {
        return  Gson().toJson(list)
    }

}
