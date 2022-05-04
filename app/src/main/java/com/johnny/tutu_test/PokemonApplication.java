package com.johnny.tutu_test;

import android.app.Application;
import android.util.Log;

import com.johnny.tutu_test.database.PokemonRepository;

public class PokemonApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceWork.setContext(this);

        deleteDB();

        PokemonRepository.initialize(this);
    }

    private void deleteDB() {
        Log.d("TAG", "Deleting DB...");
        this.deleteDatabase("pokemon_db");
    }
}
