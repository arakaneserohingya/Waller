package com.blogger.waller.room.table;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "category")
public class EntityCategory implements Serializable {

    @PrimaryKey
    @NonNull
    public String name;

    public EntityCategory(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
