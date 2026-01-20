package com.example.geolearn.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String uid;
    
    public String username;
    public String email;
    public int age;

    public User(@NonNull String uid, String username, String email, int age) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.age = age;
    }
}
