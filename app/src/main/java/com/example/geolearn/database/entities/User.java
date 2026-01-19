package com.example.geolearn.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String uid;
    
    public String name;
    public String email;

    public User(@NonNull String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }
}
