package com.movietest.displaymovie.api;


import com.movietest.displaymovie.models.Result;
import com.movietest.displaymovie.models.AllMovies;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface MovieService {

    @GET("/3/discover/movie")
    Call<AllMovies> getTopRatedMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int pageIndex,
            @Query("sort_by") String sortBy
    );

    @GET("/3/movie/{id}")
    Call<Result> getDetailsById(
            @Path("id") int id,
            @Query("api_key") String apiKey

    );

}
