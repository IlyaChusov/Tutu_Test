package com.johnny.tutu_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class PokemonActivity extends AppCompatActivity {
    public static final String POKEMON_ID = "pokemonId";
    public static final String POKEMON_URL = "pokemonUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        int pokemonId = getIntent().getIntExtra(POKEMON_ID, 0);
        ((TextView) findViewById(R.id.textView)).setText(pokemonId + "");
    }

    @NonNull
    public static Intent newIntent(Context context, int pokemonId) {
        Intent intent = new Intent(context, PokemonActivity.class);
        Log.d("TAG", "putting int to intent: " + pokemonId);
        intent.putExtra(POKEMON_ID, pokemonId);
        //intent.putExtra(POKEMON_URL, pokemon.getString("url"));

        return intent;
    }
}