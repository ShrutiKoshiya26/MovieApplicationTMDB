package com.movietest.displaymovie.api;

import static com.movietest.displaymovie.utils.UtilKeys.BASEURL;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MovieApi {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASEURL)
                    .build();
        }
        return retrofit;
    }

}
