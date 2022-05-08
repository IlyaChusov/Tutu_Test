package com.johnny.tutu_test;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.HandlerThread;
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private final Handler handler = new Handler(Looper.getMainLooper()); // TODO: Deprecated Handler
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
        reloadPokemonListFromDB(() -> {}, () -> {});
    }

    private void reloadPokemonListFromDB(@NonNull Callback callback1, @NonNull Callback callback2) {
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
            callback1.call();
            if (!pokemonList_.isEmpty()) {
                pokemonAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Произведена синхронизация с БД", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(MainActivity.this, "БД пуста", Toast.LENGTH_SHORT).show();

            callback2.call();
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
                        handler.post(() -> reloadPokemonListFromDB(() -> new FetchPokemonImages(viewModel), MainActivity.this::fetchPokemonDetails));
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
        final FetchPokemonImages imagesThread = FetchPokemonImages.get();

        public PokemonHolder(@NonNull @NotNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));

            itemView.setOnClickListener(this);
        }

        private void bind(int pokemonId, @NonNull String pokemonName) {
            //Log.d("TAG", "binding new Holder with pokemonId: " + pokemonId);
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
                                Toast.makeText(getApplicationContext(), "liveData is true, but image are missing, pokemonId: " + pokemonId, Toast.LENGTH_LONG).show();
                            else {
                                liveData.removeObservers(MainActivity.this);
                                liveData.observe(MainActivity.this, aBoolean -> {
                                    if (aBoolean) {
                                        Log.d("TAG", "Got new image to place! PokemonId: " + pokemonId);
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