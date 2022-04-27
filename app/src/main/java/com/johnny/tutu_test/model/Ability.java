package com.johnny.tutu_test.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Ability {
    @PrimaryKey
    private int abilityId;
    private String name;
    private boolean isHidden;

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
}