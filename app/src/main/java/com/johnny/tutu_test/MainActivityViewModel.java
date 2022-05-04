package com.johnny.tutu_test;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.johnny.tutu_test.model.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {
    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    private static List<Pokemon> pokemonList;

    public List<Pokemon> getPokemonList() {
        if (pokemonList == null)
            pokemonList = new ArrayList<>();
        return pokemonList;
    }

    public void setPokemonList(List<Pokemon> pokemonList) {
        MainActivityViewModel.pokemonList = pokemonList;
    }
}
