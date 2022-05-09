package com.johnny.tutu_test;

import android.app.Application;
import android.util.Log;

import com.johnny.tutu_test.database.PokemonRepository;

import java.io.File;
import java.util.Objects;

public class PokemonApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceWork.setContext(this);

        deleteDB();
        deleteImages(getFilesDir().getAbsolutePath() + "/images/thumbnails");
        deleteImages(getFilesDir().getAbsolutePath() + "/images/orig");

        PokemonRepository.initialize(this);
    }

    private void deleteDB() {
        Log.d("TAG", "Deleting DB...");
        this.deleteDatabase("pokemon_db");
    }
    private void deleteImages(String dirPath) {
        String[] children = new File(dirPath).list();
        if (children != null)
            for (String child: children)
                new File(dirPath, child).delete();
    }
}
