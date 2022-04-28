package com.johnny.tutu_test.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.johnny.tutu_test.model.PokemonAbilities;

import java.util.Date;
import java.util.List;

@Dao
public interface PokemonDAO {

    @Transaction
    @Query("SELECT * FROM pokemon")
    public LiveData<List<PokemonAbilities>> getAllPokemons();

    @Transaction
    @Query("SELECT * FROM pokemon WHERE pokemonId=(:id)")
    public LiveData<PokemonAbilities> getPokemon(int id);

    @Query("SELECT lastUpdateTime FROM DBLastUpdate WHERE actualTimeId=1")
    public LiveData<Date> getLastDBUpdateTime();
}
