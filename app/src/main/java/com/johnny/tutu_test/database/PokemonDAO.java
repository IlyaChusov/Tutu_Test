package com.johnny.tutu_test.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.johnny.tutu_test.model.Ability;
import com.johnny.tutu_test.model.Pokemon;
import com.johnny.tutu_test.model.PokemonAbilities;

import java.util.Date;
import java.util.List;

@Dao
public interface PokemonDAO {

    @Transaction
    @Query("SELECT * FROM pokemon")
    LiveData<List<PokemonAbilities>> getAllPokemons();

    @Query("SELECT * FROM pokemon")
    List<Pokemon> getAllPokemonsOnly();

    @Transaction
    @Query("SELECT * FROM pokemon WHERE pokemonId=(:id)")
    LiveData<PokemonAbilities> getPokemon(int id);

    @Query("SELECT * FROM ability")
    List<Ability> getAllAbilities();

    @Query("SELECT lastUpdateTime FROM DBLastUpdate WHERE actualTimeId=1")
    LiveData<Date> getLastDBUpdateTime();

    @Query("SELECT * FROM DBLastUpdate")
    DBLastUpdate getLastDBUpdateTime_all();

    @Insert
    void addPokemons(List<Pokemon> pokemons);

    @Insert
    void addAbilities(List<Ability> abilities);

    @Update
    void updatePokemon(Pokemon pokemon);

    @Insert
    void addLastDBUpdateTime(DBLastUpdate date);

    @Query("UPDATE DBLastUpdate SET lastUpdateTime = (:date) WHERE actualTimeId=1")
    void setLastDBUpdateTime(Date date);
}
