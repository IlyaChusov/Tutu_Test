package com.johnny.tutu_test.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.johnny.tutu_test.model.Ability;
import com.johnny.tutu_test.model.Pokemon;

@Database(entities = {Pokemon.class, Ability.class}, version = 1)
@TypeConverters(PokemonConverters.class)
abstract public class PokemonDatabase extends RoomDatabase {
    abstract public PokemonDAO pokemonDAO();
}
