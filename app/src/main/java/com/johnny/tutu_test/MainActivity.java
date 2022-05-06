package com.johnny.tutu_test;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.johnny.tutu_test.database.PokemonConverters;
import com.johnny.tutu_test.database.PokemonRepository;
import com.johnny.tutu_test.databinding.ActivityMainBinding;
import com.johnny.tutu_test.model.Ability;
import com.johnny.tutu_test.model.Pokemon;
import com.johnny.tutu_test.model.PokemonAbilities;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final PokemonAdapter pokemonAdapter = new PokemonAdapter();
    private final Handler handler = new Handler();
    private ProgressDialog progressDialog;
    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(pokemonAdapter);

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        FetchPokemonUrls fetchPokemonUrls = new FetchPokemonUrls();
        binding.reloadButton.setOnClickListener((l) -> {
            showProgressDialog("Loading Pokemons...");
            fetchPokemonUrls.start();
        });

        showProgressDialog("Getting pokemons from DB...");
        reloadPokemonListFromDB();

        binding.loadFromDBButton.setOnClickListener((l) -> {
            showProgressDialog("Getting pokemons from DB...");
            Log.d("TAG", "loading from DB...");
            reloadPokemonListFromDB();
        });

        reloadLastUpdateDBTime();
    }

    private void reloadLastUpdateDBTime() {
        final LiveData<Date> liveData = PokemonRepository.get().getLastDBUpdateTime();
        liveData.observe(this, date -> {
            if (date != null) {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
                binding.lastUpdateTime.setText(getResources().getString(R.string.last_update_time, dateFormat.format(date)));
            }

            liveData.removeObservers(MainActivity.this);
        });
    }

    private interface Callback {
        void call();
    }

    private void reloadPokemonListFromDB() {
        reloadPokemonListFromDB(() -> {});
    }

    private void reloadPokemonListFromDB(@NonNull Callback callback) {
        final LiveData<List<PokemonAbilities>> liveData = PokemonRepository.get().getAllPokemons();
        liveData.observe(this, pokemonAbilitiesList -> {
            Log.d("TAG", "Observer on rep's Pokemon list got new info");
            List<Pokemon> pokemonList_ = new ArrayList<>();
            StringBuilder s = new StringBuilder();
            for (PokemonAbilities pokemonAbility : pokemonAbilitiesList) {
                pokemonList_.add(pokemonAbility.pokemon);
                s.append(pokemonAbility.pokemon.getName()).append(", ");
            }

            viewModel.setPokemonList(pokemonList_);
            if (!pokemonList_.isEmpty()) {
                pokemonAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Произведена синхронизация с БД", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(MainActivity.this, "БД пуста", Toast.LENGTH_SHORT).show();

            callback.call();
            liveData.removeObservers(MainActivity.this);
            if (progressDialog != null)
                progressDialog.dismiss();
        });
    }

    private void showProgressDialog(@NonNull String message) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void fetchPokemonDetails() {
        new FetchPokemonDetails(viewModel.getPokemonList());
    }

    class FetchPokemonUrls extends Thread {
        String data;
        @Override
        public void run() {
            try {
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null)
                    builder.append(line);
                data = builder.toString();

                if (!data.isEmpty()) {
                    JSONObject dataObject = new JSONObject(data);
                    JSONArray dataArray = dataObject.getJSONArray("results");
                    if (dataArray.length() != 0) {

                        final List<Pokemon> pokemonList_ = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            String pokemonName = dataArray.getJSONObject(i).getString("name");
                            Pokemon pokemon = new Pokemon();
                            pokemon.setName(pokemonName);
                            pokemon.setUrl(new URL(dataArray.getJSONObject(i).getString("url")));
                            pokemonList_.add(pokemon);
                        }

                        PokemonRepository.get().addPokemons(pokemonList_, false);
                        PokemonRepository.get().setLastDBUpdateTime(new Date(), false);
                        handler.post(MainActivity.this::reloadLastUpdateDBTime);
                        handler.post(() -> reloadPokemonListFromDB(MainActivity.this::fetchPokemonDetails));
                    }
                    else throw new JSONException("pokemonList is empty");
                }
                else throw new JSONException("cannot get JSONArray");

                httpURLConnection.disconnect();
                inputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

        }
    }

    static class FetchPokemonDetails extends Thread {
        private final List<Pokemon> pokemons;
        public static final HashMap<Integer, MutableLiveData<Boolean>> detailsLoadingMap = new HashMap<>();

        public FetchPokemonDetails(List<Pokemon> pokemons) {
            this.pokemons = pokemons;

            this.start();
        }

        @Override
        public void run() {
            try {
                Log.d("TAG", "Got a request to load pokemons' details");
                Log.d("TAG", "pokemons list size: " + pokemons.size());

                for (Pokemon pokemon: pokemons)
                    detailsLoadingMap.put(pokemon.getPokemonId(), new MutableLiveData<>(false));

                final List<Ability> abilitiesInDB = PokemonRepository.get().getAllAbilities();

                for (Pokemon pokemon: pokemons) {
                    String pokemonData = loadDataFromUrl(pokemon.getUrl());

                    JSONObject jsonObject = new JSONObject(pokemonData);
                    pokemon.setBaseExperience(jsonObject.getInt("base_experience"));
                    pokemon.setHeight(jsonObject.getDouble("height"));
                    pokemon.setWeight(jsonObject.getDouble("weight"));

                    final List<Ability> abilities = new ArrayList<>();
                    JSONArray abilitiesArray = jsonObject.getJSONArray("abilities");
                    for (int i = 0; i < abilitiesArray.length(); i++) {
                        JSONObject abilityInArray = abilitiesArray.getJSONObject(i);
                        JSONObject abilityArray = abilityInArray.getJSONObject("ability");
                        String abilityName = abilityArray.getString("name");
                        boolean alreadyExists = false;
                        for (Ability ab : abilitiesInDB)
                            if (ab.getName().equals(abilityName)) {
                                alreadyExists = true;
                                break;
                            }
                        if (!alreadyExists) {
                            final Ability ability = new Ability();
                            ability.setName(abilityName);
                            ability.setHidden(abilityInArray.getBoolean("is_hidden"));
                            ability.setPokemonOwnerId(pokemon.getPokemonId());

                            abilitiesInDB.add(ability);
                            abilities.add(ability);
                        }
                    }

                    PokemonRepository.get().addAbilities(abilities, false);

                    PokemonRepository.get().updatePokemon(pokemon, false);
                    Objects.requireNonNull(detailsLoadingMap.get(pokemon.getPokemonId())).postValue(true);
                    Log.d("TAG", "pokemon with name \"" + pokemon.getName() + "\" is updated");
                }
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        @NonNull
        private String loadDataFromUrl(@NonNull URL url) throws IOException {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null)
                builder.append(line);
            String data = builder.toString();
            if (data.isEmpty())
                throw new IOException("cannot get data for pokemon");

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            return data;
        }
    }

    private class PokemonAdapter extends RecyclerView.Adapter<PokemonHolder> {
        @NonNull
        @Override
        public PokemonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PokemonHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull PokemonHolder holder, int position) {
            //Log.d("TAG", "creating onBind for position " + position + ", pokemonList size: " + viewModel.getPokemonList().size());
            //PokemonRepository.get().getAllPokemons().observe(MainActivity.this, (list) -> Log.d("TAG", list.toString()));
            Pokemon pokemon = viewModel.getPokemonList().get(position);
            holder.bind(pokemon.getPokemonId(), pokemon.getName());

        }

        @Override
        public int getItemCount() {
            return viewModel.getPokemonList().size();
        }
    }

    private class PokemonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private int pokemonId;

        public PokemonHolder(@NonNull @NotNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));

            itemView.setOnClickListener(this);
        }

        private void bind(int pokemonId, @NonNull String pokemonName) {
            //Log.d("TAG", "binding new Holder with pokemonId: " + pokemonId);
            this.pokemonId = pokemonId;

            ((TextView) itemView.findViewById(R.id.pokemonName)).setText(pokemonName);
            /*
            if (lastItem) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(35, 20, 35, 300);
                itemView.setLayoutParams(lp);
            }
             */
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(getApplicationContext(), "PogU", Toast.LENGTH_LONG).show();
            startActivity(PokemonActivity.newIntent(MainActivity.this, pokemonId));
        }
    }
}