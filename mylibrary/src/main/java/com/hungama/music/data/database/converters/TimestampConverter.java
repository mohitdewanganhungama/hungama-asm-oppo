package com.hungama.music.data.database.converters;


import androidx.room.TypeConverter;


import com.hungama.music.utils.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chetan patel
 */

public class TimestampConverter {
    static DateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS, Locale.ENGLISH);

    @TypeConverter
    public static Date fromTimestamp(String value) {
        if (value != null) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String dateToTimestamp(Date value) {

        return value == null ? null : df.format(value);
    }
}
