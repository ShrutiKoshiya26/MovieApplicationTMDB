package com.movietest.displaymovie.activity;

import static com.movietest.displaymovie.utils.UtilKeys.API_KEY;
import static com.movietest.displaymovie.utils.UtilKeys.SPKEY_SORTING;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;

import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.movietest.displaymovie.R;
import com.movietest.displaymovie.adapter.PaginationAdapter;
import com.movietest.displaymovie.api.MovieApi;
import com.movietest.displaymovie.api.MovieService;
import com.movietest.displaymovie.classes.MovieComparator;
import com.movietest.displaymovie.classes.SharedPreferenceClass;
import com.movietest.displaymovie.databinding.ActivityMainBinding;
import com.movietest.displaymovie.models.Result;
import com.movietest.displaymovie.models.AllMovies;
import com.movietest.displaymovie.utils.PaginationScrollListener;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
    public Snackbar snackbar;
    private boolean internetConnected = true;

    private static final String TAG = MainActivity.class.getName();

    PaginationAdapter adapter;
    GridLayoutManager linearLayoutManager;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES = 10;
    private int currentPage = PAGE_START;

    private MovieService movieService;
    ActivityMainBinding binding;
    private long mLastClickTime = 0;
    private int menuitemClickposition = 0;


    String sortingType = "popularity.desc";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClicks();

        //init service and load data
        movieService = MovieApi.getClient().create(MovieService.class);

        setAdapter();

    }

    private void setAdapter() {
        adapter = new PaginationAdapter(this);

        linearLayoutManager = new GridLayoutManager(this, 2);
        binding.mainRecycler.setLayoutManager(linearLayoutManager);

        binding.mainRecycler.setItemAnimator(new DefaultItemAnimator());

        binding.mainRecycler.setAdapter(adapter);

        binding.mainRecycler.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

    private void setClicks() {
        binding.backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.sortbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //prevent double click
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }

                mLastClickTime = SystemClock.elapsedRealtime();

                showPopup(binding.sortbtn);
            }
        });
    }


    public void showPopup(View v) {

        // Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(this);
        inflater.inflate(R.menu.menu_items, popup.getMenu());
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

         /*   case R.id.action_sort_moviename:

                menuitemClickposition = 0;
                sortingType = "original_title.asc";
                SharedPreferenceClass.save(this, SPKEY_SORTING, sortingType);
                loadFirstPage();
                break;
*/
            case R.id.action_sort_rating:
                Log.e(TAG, "onMenuItemClick: " + menuItem.getItemId());
                sortingType = "popularity.desc";
                menuitemClickposition = 1;
                SharedPreferenceClass.save(this, SPKEY_SORTING, sortingType);
                loadFirstPage();
                break;

            case R.id.action_sort_date:
                Log.e(TAG, "onMenuItemClick: " + menuItem.getItemId());
                sortingType = "release_date.desc";
                menuitemClickposition = 2;
                SharedPreferenceClass.save(this, SPKEY_SORTING, sortingType);
                loadFirstPage();
                break;

            default:
                break;
        }
        return false;
    }


    private void loadFirstPage() {

        callTopRatedMoviesApi(sortingType).enqueue(new Callback<AllMovies>() {
            @Override
            public void onResponse(Call<AllMovies> call, Response<AllMovies> response) {
                // Got data. Send it to adapter

                Log.e(TAG, "onMenuItemClick: " + response);

                List<Result> results = fetchResults(response);
                binding.mainProgress.setVisibility(View.GONE);
                adapter.setMovies(results);

                //  adapter.setMovies(results);

                if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<AllMovies> call, Throwable t) {
                t.printStackTrace();

            }
        });

    }


    private List<Result> fetchResults(Response<AllMovies> response) {
        AllMovies allMovies = response.body();
        return allMovies.getResults();
    }

    private void loadNextPage() {

        callTopRatedMoviesApi(sortingType).enqueue(new Callback<AllMovies>() {
            @Override
            public void onResponse(Call<AllMovies> call, Response<AllMovies> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<Result> results = fetchResults(response);

                adapter.addAll(results);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<AllMovies> call, Throwable t) {
                t.printStackTrace();

            }
        });
    }


    private Call<AllMovies> callTopRatedMoviesApi(String sortingType) {
        return movieService.getTopRatedMovies(
                API_KEY,
                "en_US",
                currentPage, sortingType
        );
    }

    //network detections

    @Override
    protected void onResume() {
        super.onResume();
        registerInternetCheckReceiver();

        sortingType = SharedPreferenceClass.read(this, SPKEY_SORTING, sortingType);

        loadFirstPage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Method to check internet connection on runtime
     */
    private void registerInternetCheckReceiver() {
        IntentFilter internetFilter = new IntentFilter();
        internetFilter.addAction("android.net.wifi.STATE_CHANGE");
        internetFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, internetFilter);
    }

    /**
     * Method to check internet connection on with receiver
     */

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = getConnectivityStatusString(context);
            setSnackbarMessage(status, false);
        }
    };

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = null;
        if (conn == TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    private void setSnackbarMessage(String status, boolean showBar) {
        String internetStatus = "";
        if (status.equalsIgnoreCase("Wifi enabled") || status.equalsIgnoreCase("Mobile data enabled")) {
            internetStatus = "Internet Connected";
        } else {
            internetStatus = "Lost Internet Connection";
        }
        snackbar = Snackbar
                .make(binding.coordinatorLayout, internetStatus, Snackbar.LENGTH_LONG)
                .setAction("Connect", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                });

        snackbar.setActionTextColor(Color.WHITE);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        if (internetStatus.equalsIgnoreCase("Lost Internet Connection")) {
            if (internetConnected) {
                snackbar.show();
                internetConnected = false;
            }
        } else {
            if (!internetConnected) {
                internetConnected = true;

                loadFirstPage();
            }
        }
    }


}
