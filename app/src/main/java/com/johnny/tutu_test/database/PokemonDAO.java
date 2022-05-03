package com.johnny.tutu_test.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.johnny.tutu_test.model.Pokemon;
import com.johnny.tutu_test.model.PokemonAbilities;

import java.util.Date;
import java.util.List;

@Dao
public interface PokemonDAO {

    @Transaction
    @Query("SELECT * FROM pokemon")
    LiveData<List<PokemonAbilities>> getAllPokemons();

    @Transaction
    @Query("SELECT * FROM pokemon WHERE pokemonId=(:id)")
    LiveData<PokemonAbilities> getPokemon(int id);

    @Query("SELECT lastUpdateTime FROM DBLastUpdate WHERE actualTimeId=1")
    LiveData<Date> getLastDBUpdateTime();

    @Insert
    void addPokemons(List<Pokemon> pokemons);

    @Update
    void updatePokemons(List<Pokemon> pokemons);

    @Update
    void updatePokemon(Pokemon pokemon);
}
