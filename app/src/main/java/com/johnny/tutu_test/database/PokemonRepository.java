package com.johnny.tutu_test.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.johnny.tutu_test.model.Ability;
import com.johnny.tutu_test.model.Pokemon;
import com.johnny.tutu_test.model.PokemonAbilities;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PokemonRepository {

    private static final String DATABASE_NAME = "pokemon_db";
    private static PokemonRepository repository;
    private static PokemonDatabase database;
    private final PokemonDAO pokemonDAO;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public static PokemonRepository get() {
        return repository;
    }

    private PokemonRepository(Context context) {
        database = Room.databaseBuilder(context, PokemonDatabase.class, DATABASE_NAME).build();
        Log.d("TAG", "DB initialized");
        pokemonDAO = database.pokemonDAO();
    }

    public static void initialize(Context context) {
        if (repository == null)
            repository = new PokemonRepository(context);
    }

    public LiveData<PokemonAbilities> getPokemon(int id) {
        return pokemonDAO.getPokemon(id);
    }

    public LiveData<List<PokemonAbilities>> getAllPokemons() {
        return pokemonDAO.getAllPokemons();
    }

    @WorkerThread
    public List<Ability> getAllAbilities() {
        return pokemonDAO.getAllAbilities();
    }

    public void addPokemons(List<Pokemon> pokemons, boolean fromMainThread) {
        if (fromMainThread)
            executor.execute(() -> pokemonDAO.addPokemons(pokemons));
        else
            pokemonDAO.addPokemons(pokemons);
    }

    public void updatePokemons(List<Pokemon> pokemons, boolean fromMainThread) {
        if (fromMainThread)
            executor.execute(() -> pokemonDAO.updatePokemons(pokemons));
        else
            pokemonDAO.updatePokemons(pokemons);
    }

    public void updatePokemon(Pokemon pokemon, boolean fromMainThread) {
        if (fromMainThread)
            executor.execute(() -> pokemonDAO.updatePokemon(pokemon));
        else
            pokemonDAO.updatePokemon(pokemon);
    }

    public void addAbilities(List<Ability> abilities, boolean fromMainThread) {
        if (fromMainThread)
            executor.execute(() -> pokemonDAO.addAbilities(abilities));
        else
            pokemonDAO.addAbilities(abilities);
    }
}
