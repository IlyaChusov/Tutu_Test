package com.johnny.tutu_test.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {@ForeignKey(entity = Pokemon.class, parentColumns = "pokemonId", childColumns = "pokemonOwnerId", onDelete = 5, onUpdate = 5)})
public class Ability {
    @PrimaryKey(autoGenerate = true)
    private int abilityId;
    private String name;
    private boolean isHidden;
    private int pokemonOwnerId;

    public int getAbilityId() {
        return abilityId;
    }

    public void setAbilityId(int abilityId) {
        this.abilityId = abilityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public int getPokemonOwnerId() {
        return pokemonOwnerId;
    }

    public void setPokemonOwnerId(int pokemonOwnerId) {
        this.pokemonOwnerId = pokemonOwnerId;
    }

}