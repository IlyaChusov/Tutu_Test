package com.johnny.tutu_test.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.net.URL;

@Entity
public class Pokemon {
    @PrimaryKey(autoGenerate = true)
    private int pokemonId;
    private String name;
    private URL url;
    private int baseExperience;
    private double height;
    private double weight;

    public int getPokemonId() {
        return pokemonId;
    }
    public void setPokemonId(int pokemonId) {
        this.pokemonId = pokemonId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }
    public void setUrl(URL url) {
        this.url = url;
    }

    public int getBaseExperience() {
        return baseExperience;
    }
    public void setBaseExperience(int baseExperience) {
        this.baseExperience = baseExperience;
    }

    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }
}