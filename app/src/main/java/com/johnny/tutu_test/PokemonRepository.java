package com.johnny.tutu_test;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.johnny.tutu_test.database.PokemonDAO;
import com.johnny.tutu_test.database.PokemonDatabase;
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
        Log.d("TAG", "Initializing DB...");
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
        Log.d("TAG", "getting all pokemons from repository...");
        return pokemonDAO.getAllPokemons();
    }

    public void addPokemons(List<Pokemon> pokemons) {
        Log.d("TAG", "New pokemons are added to DB");
        executor.execute(() -> pokemonDAO.addPokemons(pokemons));
    }

    public void updatePokemons(List<Pokemon> pokemons) {
        Log.d("TAG", "Pokemons are updating...");
        executor.execute(() -> pokemonDAO.updatePokemons(pokemons));
    }
}
