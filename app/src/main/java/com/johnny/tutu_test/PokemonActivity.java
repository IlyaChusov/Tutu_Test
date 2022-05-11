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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.johnny.tutu_test.database.PokemonRepository;
import com.johnny.tutu_test.model.Ability;
import com.johnny.tutu_test.model.Pokemon;
import com.johnny.tutu_test.model.PokemonAbilities;

import java.io.File;
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
        final FetchPokemonDetails detailsThread = FetchPokemonDetails.get();
        final FetchPokemonImages imagesThread = FetchPokemonImages.get();
        if (detailsThread != null) {
            MutableLiveData<Boolean> liveData = detailsThread.getDetailsLoadingMap().get(pokemonId);
            if (liveData != null) {
                Object value = liveData.getValue();
                if (value != null)
                    if ((boolean) value)
                        placeTextData(pokemonId);
                    else {
                        ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setMessage(getResources().getString(R.string.waiting_for_pokemon_to_load));
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setOnCancelListener(listener -> finish());
                        progressDialog.show();

                        liveData.observe(this, aBoolean -> {
                            if (aBoolean) {
                                placeTextData(pokemonId);
                                liveData.removeObservers(PokemonActivity.this);
                                progressDialog.dismiss();
                            }
                        });
                    }
            }
        }
        else
            placeTextData(pokemonId);

        final ImageView pokemonImage = findViewById(R.id.pokemonImage);
        if (!placeImage(pokemonId, pokemonImage))
            if (imagesThread != null) {
                MutableLiveData<Boolean> liveData = imagesThread.getImagesLoadingMap().get(pokemonId);
                if (liveData != null) {
                    Object value = liveData.getValue();
                    if (value != null)
                        if ((boolean) value)
                            placeImage(pokemonId, pokemonImage);
                        else {
                            liveData.observe(this, aBoolean -> {
                                if (aBoolean) {
                                    placeImage(pokemonId, pokemonImage);
                                    liveData.removeObservers(PokemonActivity.this);
                                }
                            });
                        }
                }
            }
    }

    private void placeTextData(int pokemonId) {
        ListView abilitiesList = findViewById(R.id.abilitiesList);
        TextView pokemonName = findViewById(R.id.pokemonNameActivity);
        TextView pokemonExp = findViewById(R.id.pokemonExp);
        TextView pokemonHeight = findViewById(R.id.pokemonHeight);
        TextView pokemonWeight = findViewById(R.id.pokemonWeight);

        final LiveData<PokemonAbilities> pokemonLiveData = PokemonRepository.get().getPokemon(pokemonId);
        pokemonLiveData.observeForever(new Observer<PokemonAbilities>() {
            @Override
            public void onChanged(PokemonAbilities pokemonAbilities) {
                Pokemon pokemon = pokemonAbilities.pokemon;
                List<Ability> abilities = pokemonAbilities.abilities;
                if (pokemon == null || abilities == null)
                    return;

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

    private boolean placeImage(int pokemonId, @NonNull ImageView pokemonImage) {
        final File imageFile = new File(getFilesDir().getAbsolutePath() + "/images/orig", "pok_id_" + pokemonId + ".png");
        if (imageFile.exists()) {
            final Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (image != null) {
                pokemonImage.setImageBitmap(image);
                return true;
            }
        }
        return false;
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
        intent.putExtra(POKEMON_ID, pokemonId);
        return intent;
    }
}