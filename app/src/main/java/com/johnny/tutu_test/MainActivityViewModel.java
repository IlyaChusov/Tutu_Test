package com.johnny.tutu_test;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.util.Dictionary;

public class MainActivityViewModel extends AndroidViewModel {
    private static Dictionary<String, MutableLiveData<JSONObject>> pokemonDict;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public Dictionary<String, MutableLiveData<JSONObject>> getPokemonDict() {
        return pokemonDict;
    }

    public void setPokemonDict(Dictionary<String, MutableLiveData<JSONObject>> pokemonDict_) {
        pokemonDict = pokemonDict_;
    }
}
