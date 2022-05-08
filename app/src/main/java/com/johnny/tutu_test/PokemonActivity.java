package com.johnny.tutu_test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.johnny.tutu_test.database.PokemonRepository;
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
        FetchPokemonDetails detailsThread = FetchPokemonDetails.get();
        if (detailsThread != null) {
            MutableLiveData<Boolean> liveData = detailsThread.getDetailsLoadingMap().get(pokemonId);
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
        else
            placeAllData(pokemonId);
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
                if (abilities.isEmpty())
                    ((TextView) findViewById(R.id.abilitiesText)).setText(resources.getString(R.string.abilities_list_placing, "empty"));
                else
                    abilitiesList.setAdapter(new AbilitiesAdapter(PokemonActivity.this, abilities));
                pokemonLiveData.removeObserver(this);
            }
        });
    }

    private static class AbilitiesAdapter extends ArrayAdapter<Ability> {
        private static final int resource = android.R.layout.simple_list_item_2;
        private static List<Ability> list;

        AbilitiesAdapter(@NonNull Context context, @NonNull List<Ability> list) {
            super(context, resource, list);
            AbilitiesAdapter.list = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            AbilityHolder abilityHolder;
            final Context context = getContext();
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(resource, null);
                abilityHolder = new AbilityHolder(convertView);
                convertView.setTag(abilityHolder);
            }
            else
                abilityHolder = (AbilityHolder) convertView.getTag();

            Ability ability = list.get(position);
            abilityHolder.abilityNameView.setText(ability.getName());
            abilityHolder.abilityHiddenView.setText(context.getResources().getString(R.string.ability_hidden, ability.isHidden()));

            return convertView;
        }

        private static class AbilityHolder {
            final TextView abilityNameView, abilityHiddenView;

            AbilityHolder(@NonNull View view) {
                this.abilityNameView = view.findViewById(android.R.id.text1);
                this.abilityHiddenView = view.findViewById(android.R.id.text2);
            }
        }
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

        return intent;
    }
}