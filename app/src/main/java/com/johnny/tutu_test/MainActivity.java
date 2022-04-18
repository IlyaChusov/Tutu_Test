package com.johnny.tutu_test;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.johnny.tutu_test.databinding.ActivityMainBinding;

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
import java.util.Dictionary;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private ArrayList<Pair<String, String>> pokemonUrls = new ArrayList<>();
    private PokemonAdapter pokemonAdapter = new PokemonAdapter();
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
        Dictionary<String, MutableLiveData<JSONObject>> pokemonDict_ = viewModel.getPokemonDict();
        if (pokemonDict_ == null)
            viewModel.setPokemonDict(new Hashtable<>());

        FetchPokemonUrls fetchPokemonUrls = new FetchPokemonUrls();
        binding.reloadButton.setOnClickListener((l) -> {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading Pokemons...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            fetchPokemonUrls.start();
        });

    }

    public void fetchPokemonDetails() {
        Toast.makeText(getApplicationContext(), "fetchPokemonDetails called", Toast.LENGTH_LONG).show();
        for (int i = 0; i < pokemonUrls.size(); i++) {
            new FetchPokemonDetails(pokemonUrls.get(i).first, pokemonUrls.get(i).second);
            int finalI = i;
            viewModel.getPokemonDict().get(pokemonUrls.get(i).first + "").observeForever(
                    new Observer<JSONObject>() {
                        @Override
                        public void onChanged(JSONObject jsonObject) {

                            viewModel.getPokemonDict().get(pokemonUrls.get(finalI).first + "").removeObserver(this);
                        }
                    }


            );
        }
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
                        for (int i = 0; i < dataArray.length(); i++) {
                            String pokemonName = dataArray.getJSONObject(i).getString("name");
                            pokemonUrls.add(new Pair<>(pokemonName, dataArray.getJSONObject(i).getString("url")));
                            viewModel.getPokemonDict().put(pokemonName, new MutableLiveData<>());
                        }
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
        private final String pokemonName;
        private final String pokemonUrl;
        public FetchPokemonDetails(String pokemonName, String pokemonUrl) {
            this.pokemonName = pokemonName;
            this.pokemonUrl = pokemonUrl;

            this.start();
        }

        @Override
        public void run() {
            try {
                URL url = new URL(pokemonUrl);
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

                JSONObject jsonObject = new JSONObject(data);
                viewModel.getPokemonDict().get(pokemonName).postValue(jsonObject);
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
            holder.bind(pokemonUrls.get(position).first, position == pokemonUrls.size() - 1);

        }

        @Override
        public int getItemCount() {
            return pokemonUrls.size();
        }
    }

    private class PokemonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private String pokemonName;

        public PokemonHolder(@NonNull @NotNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));

            itemView.setOnClickListener(this);
        }

        private void bind(@NonNull String pokemonName, boolean lastItem) {
            this.pokemonName = pokemonName;

            ((TextView) itemView.findViewById(R.id.pokemonName)).setText(pokemonName);
            if (lastItem) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(35, 20, 35, 300);
                itemView.setLayoutParams(lp);
            }
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "PogU", Toast.LENGTH_LONG).show();
            try {
                startActivity(PokemonActivity.newIntent(MainActivity.this, null));
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "KEKHands", Toast.LENGTH_LONG).show();
            }
        }
    }
}