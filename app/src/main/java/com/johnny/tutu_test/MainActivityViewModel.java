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

    private List<Pokemon> pokemonList;
    private String lastUpdateTime;
    private boolean hasData = false;

    public List<Pokemon> getPokemonList() {
        if (pokemonList == null)
            pokemonList = new ArrayList<>();
        return pokemonList;
    }

    public void setPokemonList(List<Pokemon> pokemonList) {
        this.pokemonList = pokemonList;
        hasData = true;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        hasData = true;
    }

    public boolean hasData() {
        return hasData;
    }
}
