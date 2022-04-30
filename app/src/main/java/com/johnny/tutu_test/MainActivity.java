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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.johnny.tutu_test.databinding.ActivityMainBinding;
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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
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

        // TODO: убрать pokemonDict
        if (viewModel.getPokemonDict() == null)
            viewModel.setPokemonDict(new Hashtable<>());

        FetchPokemonUrls fetchPokemonUrls = new FetchPokemonUrls();
        binding.reloadButton.setOnClickListener((l) -> {
            showProgressDialog("Loading Pokemons...");
            fetchPokemonUrls.start();
        });

        binding.loadFromDBButton.setOnClickListener((l) -> {
            showProgressDialog("Getting pokemons from DB...");
            LiveData<List<PokemonAbilities>> liveData = PokemonRepository.get().getAllPokemons();
            liveData.observeForever(new Observer<List<PokemonAbilities>>() {
                @Override
                public void onChanged(List<PokemonAbilities> pokemonAbilitiesList) {
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
                        Log.d("TAG", "got pokemonList: " + s);
                        Toast.makeText(MainActivity.this, "Произведена синхронизация с БД", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MainActivity.this, "БД пуста", Toast.LENGTH_SHORT).show();

                    //liveData.removeObserver(this);
                    Log.d("TAG", "List from rep has observer: " + liveData.hasObservers());
                    progressDialog.dismiss();
                }
            });
        });
    }

    private void showProgressDialog(@NonNull String message) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void fetchPokemonDetails() {
        List<Pokemon> pokemonList_ = viewModel.getPokemonList();
        for (int i = 0; i < pokemonList_.size(); i++) {
            final Pokemon pokemon = pokemonList_.get(i);
            viewModel.getPokemonDict().get(pokemon.getName()).observeForever(
                    new Observer<JSONObject>() {
                        @Override
                        public void onChanged(JSONObject jsonObject) {

                            Log.d("TAG", "PokemonDict from viewModel got new JSONObject: " + jsonObject);
                            viewModel.getPokemonDict().get(pokemon.getName()).removeObserver(this);
                        }
                    }


            );
        }
        new FetchPokemonDetails(pokemonList_);
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
                            viewModel.getPokemonDict().put(pokemonName, new MutableLiveData<>());
                        }

                        PokemonRepository.get().addPokemons(pokemonList_);
                        viewModel.setPokemonList(pokemonList_);
                        handler.post(() -> pokemonAdapter.notifyDataSetChanged());

                        progressDialog.dismiss();
                        handler.post(MainActivity.this::fetchPokemonDetails);
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

    class FetchPokemonDetails extends Thread {
        private final List<Pokemon> pokemons;
        public FetchPokemonDetails(List<Pokemon> pokemons) {
            this.pokemons = pokemons;

            this.start();
        }

        @Override
        public void run() {
            try {
                Log.d("TAG", "Got a request to load pokemons' details");
                for (Pokemon pokemon: pokemons) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) pokemon.getUrl().openConnection();
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

                    JSONObject jsonObject = new JSONObject(data);
                    pokemon.setBaseExperience(jsonObject.getInt("base_experience"));
                    pokemon.setHeight(jsonObject.getDouble("height"));
                    pokemon.setWeight(jsonObject.getDouble("weight"));

                    Log.d("TAG", "pokemon with name \"" + pokemon.getName() + "\" is updated");
                    //viewModel.getPokemonDict().get(pokemonName).postValue(jsonObject);
                }
                PokemonRepository.get().updatePokemons(pokemons);
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
            }
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
            Log.d("TAG", "creating onBind for position " + position + ", pokemonList size: " + viewModel.getPokemonList().size());
            PokemonRepository.get().getAllPokemons().observe(MainActivity.this, (list) -> Log.d("TAG", list.toString()));
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
            Log.d("TAG", "binding new Holder with pokemonId: " + pokemonId);
            this.pokemonId = pokemonId;

            ((TextView) itemView.findViewById(R.id.pokemonName)).setText(pokemonName);
            //if (lastItem) {
            //    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //    lp.setMargins(35, 20, 35, 300);
            //    itemView.setLayoutParams(lp);
            //}
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "PogU", Toast.LENGTH_LONG).show();
            startActivity(PokemonActivity.newIntent(MainActivity.this, pokemonId));
        }
    }
}