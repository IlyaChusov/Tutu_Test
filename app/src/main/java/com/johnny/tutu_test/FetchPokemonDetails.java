package com.johnny.tutu_test;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;

import com.johnny.tutu_test.database.PokemonRepository;
import com.johnny.tutu_test.model.Ability;
import com.johnny.tutu_test.model.Pokemon;

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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FetchPokemonDetails extends Thread {
    private final List<Pokemon> pokemons;
    private final HashMap<Integer, MutableLiveData<Boolean>> detailsLoadingMap = new HashMap<>();
    private static FetchPokemonDetails thread;

    public FetchPokemonDetails(@NonNull List<Pokemon> pokemons) {
        super("PokemonDetails Thread");
        this.pokemons = pokemons;
        thread = this;
        for (Pokemon pokemon: pokemons)
            detailsLoadingMap.put(pokemon.getPokemonId(), new MutableLiveData<>(false));

        this.start();
    }

    @Nullable
    public static FetchPokemonDetails get() {
        return thread;
    }

    public HashMap<Integer, MutableLiveData<Boolean>> getDetailsLoadingMap() {
        return detailsLoadingMap;
    }

    @Override
    public void run() {
        try {
            Log.d("TAG", "Got a request to load pokemons' details");
            Log.d("TAG", "pokemons list size: " + pokemons.size());

            final List<Ability> abilitiesInDB = PokemonRepository.get().getAllAbilities();
            FetchPokemonImages imagesThread = FetchPokemonImages.get();

            for (Pokemon pokemon: pokemons) {
                String pokemonData = loadDataFromUrl(pokemon.getUrl());

                JSONObject jsonObject = new JSONObject(pokemonData);
                pokemon.setBaseExperience(jsonObject.getInt("base_experience"));
                pokemon.setHeight(jsonObject.getDouble("height"));
                pokemon.setWeight(jsonObject.getDouble("weight"));
                pokemon.setImageURL(new URL(jsonObject.getJSONObject("sprites").getJSONObject("other").getJSONObject("home").getString("front_default")));

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

                if (imagesThread != null)
                    imagesThread.loadImage(pokemon.getPokemonId(), pokemon.getImageURL());
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @WorkerThread
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
