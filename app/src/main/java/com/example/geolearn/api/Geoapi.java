package com.example.geolearn.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Geoapi {

    @GET("api.php")
    Call<TriviaResponse> getQuestions(
            @Query("amount") int amount,
            @Query("category") int category,
            @Query("difficulty") String difficulty,
            @Query("type") String type
    );

    @GET("v3.1/all?fields=name,flags,capital,region")
    Call<List<Country>> getCountries();
}
