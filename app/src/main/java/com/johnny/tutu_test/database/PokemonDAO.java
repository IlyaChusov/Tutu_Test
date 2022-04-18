package com.johnny.tutu_test.database;

import androidx.room.Dao;
import androidx.room.Query;

import com.johnny.tutu_test.model.Pokemon;

import java.util.List;

@Dao
public interface PokemonDAO {

    @Query("SELECT * FROM pokemons")
    public List<Pokemon> getAllPokemons();

    @Query("SELECT * FROM pokemons WHERE id=(:id)")
    public Pokemon getPokemon(int id);
}
