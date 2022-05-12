package com.johnny.tutu_test;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.johnny.tutu_test.database.PokemonRepository;
import com.johnny.tutu_test.model.Pokemon;
import com.johnny.tutu_test.model.PokemonAbilities;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final PokemonAdapter pokemonAdapter = new PokemonAdapter();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ProgressDialog progressDialog;
    private MainActivityViewModel viewModel;
    private TextView lastUpdateTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(pokemonAdapter);
        lastUpdateTimeView = findViewById(R.id.last_update_time);
        findViewById(R.id.reloadButton).setOnClickListener((l) -> {
            showProgressDialog(R.string.loading_pokemons);
            FetchPokemonUrls fetchPokemonUrls = new FetchPokemonUrls();
            fetchPokemonUrls.start();
        });
        if (savedInstanceState == null)
            loadAllData();
        else if (!viewModel.hasData())
            loadAllData();
        String lastUpdateTime = viewModel.getLastUpdateTime();
        if (lastUpdateTime != null)
            lastUpdateTimeView.setText(getResources().getString(R.string.last_update_time, lastUpdateTime));
    }

    private void loadAllData() {
        showProgressDialog(R.string.getting_pokemons_from_db);
        reloadPokemonListFromDB();
        reloadLastUpdateDBTime();
    }

    private void reloadLastUpdateDBTime() {
        final LiveData<Date> liveData = PokemonRepository.get().getLastDBUpdateTime();
        liveData.observe(this, date -> {
            if (date != null) {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
                String time = dateFormat.format(date);
                viewModel.setLastUpdateTime(time);
                lastUpdateTimeView.setText(getResources().getString(R.string.last_update_time, time));
            }
            if (progressDialog != null)
                progressDialog.dismiss();
            liveData.removeObservers(MainActivity.this);
        });
    }

    private interface Callback {
        void call();
    }

    private void reloadPokemonListFromDB() {
        reloadPokemonListFromDB(() -> {}, () -> {});
    }

    private void reloadPokemonListFromDB(@NonNull Callback callback1, @NonNull Callback callback2) {
        final LiveData<List<PokemonAbilities>> liveData = PokemonRepository.get().getAllPokemons();
        liveData.observe(this, pokemonAbilitiesList -> {
            List<Pokemon> pokemonList = new ArrayList<>();
            for (PokemonAbilities pokemonAbility : pokemonAbilitiesList)
                pokemonList.add(pokemonAbility.pokemon);

            viewModel.setPokemonList(pokemonList);
            callback1.call();
            if (!pokemonList.isEmpty()) {
                pokemonAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, R.string.db_sync_is_done, Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(MainActivity.this, R.string.db_is_empty, Toast.LENGTH_SHORT).show();

            callback2.call();
            liveData.removeObservers(MainActivity.this);
            if (progressDialog != null)
                progressDialog.dismiss();
        });
    }

    private void showProgressDialog(@StringRes int messageResId) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getResources().getString(messageResId));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void fetchPokemonDetails() {
        new FetchPokemonDetails(viewModel.getPokemonList());
    }

    private class FetchPokemonUrls extends Thread {
        public FetchPokemonUrls() {
            super("PokemonUrls Thread");
        }

        @Override
        public void run() {
            try {
                String data;
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

                        final PokemonRepository pokemonRepository = PokemonRepository.get();
                        final List<Pokemon> pokemonsFromDB = pokemonRepository.getAllPokemonsOnly();
                        final List<Pokemon> pokemonListToBeAdded = new ArrayList<>();

                        for (int i = 0; i < dataArray.length(); i++) {
                            String pokemonName = dataArray.getJSONObject(i).getString("name");
                            if (!pokemonListHasPokemon(pokemonsFromDB, pokemonName)) {
                                Pokemon pokemon = new Pokemon();
                                pokemon.setName(pokemonName);
                                pokemon.setUrl(new URL(dataArray.getJSONObject(i).getString("url")));
                                pokemonListToBeAdded.add(pokemon);
                            }
                        }
                        if (!pokemonListToBeAdded.isEmpty()) {
                            pokemonRepository.addPokemons(pokemonListToBeAdded, false);
                            handler.post(() -> reloadPokemonListFromDB(() -> new FetchPokemonImages(viewModel), MainActivity.this::fetchPokemonDetails));
                        }
                        else
                            handler.post(() -> Toast.makeText(MainActivity.this, R.string.db_is_updated_already, Toast.LENGTH_SHORT).show());
                        pokemonRepository.setLastDBUpdateTime(new Date(), false);
                        handler.post(MainActivity.this::reloadLastUpdateDBTime);
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

        private boolean pokemonListHasPokemon(@NonNull List<Pokemon> pokemonList, @NonNull String pokemonName) {
            for (Pokemon pokemon: pokemonList)
                if (pokemon.getName().equals(pokemonName))
                    return true;
            return false;
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
        final FetchPokemonImages imagesThread = FetchPokemonImages.get();

        public PokemonHolder(@NonNull @NotNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));

            itemView.setOnClickListener(this);
        }

        private void bind(int pokemonId, @NonNull String pokemonName) {
            this.pokemonId = pokemonId;
            ((TextView) itemView.findViewById(R.id.pokemonName)).setText(pokemonName);
            final ImageView imageView = itemView.findViewById(R.id.thumbnail);
            imageView.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_android_black));

            File imageFile = new File(getFilesDir().getAbsolutePath() + "/images/thumbnails", "pok_id_" + pokemonId + "_thumb.png");
            if (imageFile.exists()) {
                Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (image != null)
                    imageView.setImageBitmap(image);
            }
            else {
                if (imagesThread != null) {
                    MutableLiveData<Boolean> liveData = imagesThread.getImagesLoadingMap().get(pokemonId);
                    if (liveData != null) {
                        Object value = liveData.getValue();
                        if (value != null) {
                            if ((boolean) value)
                                Log.e("TAG", "liveData is true, but image are missing, pokemonId: " + pokemonId);
                            else {
                                liveData.removeObservers(MainActivity.this);
                                liveData.observe(MainActivity.this, aBoolean -> {
                                    if (aBoolean) {
                                        File imageFile_ = new File(getFilesDir().getAbsolutePath() + "/images/thumbnails", "pok_id_" + pokemonId + "_thumb.png");
                                        if (imageFile_.exists()) {
                                            Bitmap image = BitmapFactory.decodeFile(imageFile_.getAbsolutePath());
                                            if (image != null)
                                                imageView.setImageBitmap(image);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onClick(View v) {
            startActivity(PokemonActivity.newIntent(MainActivity.this, pokemonId));
        }
    }
}