package com.movietest.displaymovie.activity;

import static com.movietest.displaymovie.utils.UtilKeys.API_KEY;
import static com.movietest.displaymovie.utils.UtilKeys.BASE_URL_IMG;
import static com.movietest.displaymovie.utils.UtilKeys.INTENTKEY_MOVIEID;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.movietest.displaymovie.R;
import com.movietest.displaymovie.api.MovieApi;
import com.movietest.displaymovie.api.MovieService;
import com.movietest.displaymovie.databinding.ActivityDetailsBinding;
import com.movietest.displaymovie.models.Result;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {


    private int id = 0;
    private MovieService movieService;
    private static final String TAG = DetailsActivity.class.getName();

    ActivityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClicks();

        getDeatilsData();
        movieService = MovieApi.getClient().create(MovieService.class);
        loadFirstPage();


    }

    private void setClicks() {
        binding.backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void getDeatilsData() {

        Bundle extras = getIntent().getExtras();

        String mId = "";

        if (extras != null) {
            mId = extras.getString(INTENTKEY_MOVIEID);

        }
        id = Integer.parseInt(mId);

    }

    private void loadFirstPage() {
        Call<Result> call = movieService.getDetailsById(
                id,
                API_KEY
        );

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try {

                    Glide
                            .with(getBaseContext())
                            .load(BASE_URL_IMG + response.body().getPosterPath())
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                            .into(binding.imgPoster);
                    binding.yearTxt.setText(response.body().getReleaseDate().substring(0, 4)  // we want the year only
                            + " | "
                            + response.body().getOriginalLanguage().toUpperCase());
                    binding.titleTxt.setText(response.body().getTitle());
                    binding.descTxt.setText(response.body().getOverview());

                    binding.mainProgress.setVisibility(View.GONE);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }


}
