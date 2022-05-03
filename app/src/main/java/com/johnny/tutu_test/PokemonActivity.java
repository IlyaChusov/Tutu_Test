package com.johnny.tutu_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.johnny.tutu_test.model.Ability;
import com.johnny.tutu_test.model.Pokemon;
import com.johnny.tutu_test.model.PokemonAbilities;

import java.util.List;

public class PokemonActivity extends AppCompatActivity {
    public static final String POKEMON_ID = "pokemonId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        int pokemonId = getIntent().getIntExtra(POKEMON_ID, 0);
        MutableLiveData<Boolean> liveData = MainActivity.FetchPokemonDetails.detailsLoadingMap.get(pokemonId);
        if (liveData != null) {
            Object value = liveData.getValue();
            if (value != null)
                if ((boolean) value)
                    placeAllData(pokemonId);
                else {
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Waiting for pokemon to load...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    liveData.observe(this, aBoolean -> {
                        if (aBoolean) {
                            Log.d("TAG", "Got a pokemon for PokemonActivity, updating...");
                            placeAllData(pokemonId);
                            liveData.removeObservers(PokemonActivity.this);
                            progressDialog.dismiss();
                        }
                    });
                }
        }





    }

    private void placeAllData(int pokemonId) {
        ListView abilitiesList = findViewById(R.id.abilitiesList);
        TextView pokemonName = findViewById(R.id.pokemonNameActivity);
        TextView pokemonExp = findViewById(R.id.pokemonExp);
        TextView pokemonHeight = findViewById(R.id.pokemonHeight);
        TextView pokemonWeight = findViewById(R.id.pokemonWeight);

        final LiveData<PokemonAbilities> pokemonLiveData = PokemonRepository.get().getPokemon(pokemonId);
        pokemonLiveData.observeForever(new Observer<PokemonAbilities>() {
            @Override
            public void onChanged(PokemonAbilities pokemonAbilities) {
                Log.d("TAG", "Got new info to load in PokemonActivity");

                Pokemon pokemon = pokemonAbilities.pokemon;
                List<Ability> abilities = pokemonAbilities.abilities;

                Resources resources = getResources();
                pokemonName.setText(pokemon.getName());
                pokemonExp.setText(resources.getString(R.string.pokemon_exp, pokemon.getBaseExperience()));
                pokemonHeight.setText(resources.getString(R.string.pokemon_height, pokemon.getHeight()));
                pokemonWeight.setText(resources.getString(R.string.pokemon_weight, pokemon.getWeight()));
                abilitiesList.setAdapter(new ArrayAdapter<>(PokemonActivity.this, android.R.layout.simple_list_item_1, abilities));
                pokemonLiveData.removeObserver(this);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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