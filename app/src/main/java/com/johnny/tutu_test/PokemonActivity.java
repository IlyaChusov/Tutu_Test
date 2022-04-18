package com.johnny.tutu_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class PokemonActivity extends AppCompatActivity {
    public static final String POKEMON_NAME = "pokemonName";
    public static final String POKEMON_URL = "pokemonUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        String pokemonName = getIntent().getStringExtra(POKEMON_NAME);
        ((TextView) findViewById(R.id.textView)).setText(pokemonName);
    }

    @NonNull
    public static Intent newIntent(Context context, @NonNull JSONObject pokemon) throws JSONException {
        Intent intent = new Intent(context, PokemonActivity.class);
        intent.putExtra(POKEMON_NAME, pokemon.getString("name"));
        intent.putExtra(POKEMON_URL, pokemon.getString("url"));

        return intent;
    }
}