package com.johnny.tutu_test.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class PokemonAbilities {
    @Embedded
    public Pokemon pokemon;

    @Relation(parentColumn = "pokemonId", entityColumn = "abilityId")
    public List<Ability> abilities;
}
