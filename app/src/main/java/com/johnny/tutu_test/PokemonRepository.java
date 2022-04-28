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

public class PokemonRepository {

    private static final String DATABASE_NAME = "pokemon_db";
    private static PokemonRepository repository;
    private static PokemonDatabase database;

    public static PokemonRepository get() {
        return repository;
    }

    private PokemonRepository(Context context) {
        Log.d("TAG", "Initializing DB...");
        database = Room.databaseBuilder(context, PokemonDatabase.class, DATABASE_NAME).build();
        Log.d("TAG", "DB initialized");
    }

    public static void initialize(Context context) {
        if (repository == null)
            repository = new PokemonRepository(context);
    }

    public PokemonDAO pokemonDAO() {
        return database.pokemonDAO();
    }

    public LiveData<PokemonAbilities> getPokemon(int id) {
        return database.pokemonDAO().getPokemon(id);
    }

    public LiveData<List<PokemonAbilities>> getAllPokemons() {
        return database.pokemonDAO().getAllPokemons();
    }
}
