package com.johnny.tutu_test;

import android.app.Application;
import com.johnny.tutu_test.database.PokemonRepository;

public class PokemonApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PokemonRepository.initialize(this);
    }
}
