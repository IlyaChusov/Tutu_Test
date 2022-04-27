package com.johnny.tutu_test.database;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import com.johnny.tutu_test.model.Ability;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
