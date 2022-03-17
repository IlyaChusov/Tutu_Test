package com.johnny.tutu_test;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private JSONArray pokemonList = new JSONArray();
    private PokemonAdapter pokemonAdapter = new PokemonAdapter();
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(pokemonAdapter);


        FetchData fetchData = new FetchData();
        fetchData.start();


    }

    class FetchData extends Thread {
        String data;
        @Override
        public void run() {
            try {
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null)
                    builder.append(line);
                data = builder.toString();

                if (!data.isEmpty()) {
                    JSONObject dataObject = new JSONObject(data);
                    JSONArray dataArray = dataObject.getJSONArray("results");
                    if (dataArray.length() != 0) {
                        pokemonList = dataArray;
                        handler.post(() -> pokemonAdapter.notifyDataSetChanged());

                    }
                    else throw new JSONException("pokemonList is empty");
                }
                else {
                    throw new JSONException("cannot get JSONArray");
                }

            } catch (IOException | JSONException e) {
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
            try {
                holder.bind(pokemonList.getJSONObject(position));
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "KEKHands", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public int getItemCount() {
            return pokemonList.length();
        }
    }

    private class PokemonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private JSONObject pokemon;

        public PokemonHolder(@NonNull @NotNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));

            itemView.setOnClickListener(this);
        }

        private void bind(@NonNull JSONObject pokemon) {
            this.pokemon = pokemon;

            try {
                ((TextView) itemView.findViewById(R.id.pokemonName)).setText(pokemon.getString("name"));
            }
            catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "KEKHands", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "PogU", Toast.LENGTH_LONG).show();
        }
    }
}