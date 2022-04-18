package com.johnny.tutu_test.database;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import java.net.MalformedURLException;
import java.net.URL;

public class PokemonConverters {

    @TypeConverter
    public String fromURL(@NonNull URL url) {
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
}
