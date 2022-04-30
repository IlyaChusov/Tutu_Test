package com.johnny.tutu_test;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.johnny.tutu_test.model.Pokemon;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {
    private static Dictionary<String, MutableLiveData<JSONObject>> pokemonDict;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public Dictionary<String, MutableLiveData<JSONObject>> getPokemonDict() {
        return pokemonDict;
    }

    public void setPokemonDict(Dictionary<String, MutableLiveData<JSONObject>> pokemonDict) {
        MainActivityViewModel.pokemonDict = pokemonDict;
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
