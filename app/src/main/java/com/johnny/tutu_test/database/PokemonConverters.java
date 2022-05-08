package com.johnny.tutu_test.database;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PokemonConverters {

    @TypeConverter
    public String fromURL(URL url) {
        if (url == null)
            return "";
        return url.toString();
    }

    @TypeConverter
    public URL toURL(String urlStr) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            return null;
        }
        return url;
    }

    @TypeConverter
    public String fromDate(@NonNull Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    @TypeConverter
    public Date toDate(String dateStr) {
        Date date;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return new Date();
        }
        return date;
    }
}
