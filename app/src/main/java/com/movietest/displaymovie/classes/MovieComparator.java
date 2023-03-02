package com.movietest.displaymovie.classes;

import com.movietest.displaymovie.models.Result;

import java.util.Comparator;

public class MovieComparator implements Comparator<Result> {
    int movieItemClick;

    public MovieComparator(int movieItemClick) {
        this.movieItemClick = movieItemClick;
    }

    @Override
    public int compare(Result movie1, Result movie2) {
        if (movieItemClick == 0) {
            return movie1.getTitle().compareTo(movie2.getTitle());
        } else if (movieItemClick == 1) {
            return Double.compare(movie2.getVoteAverage(), movie1.getVoteAverage());
        } else {
            return movie1.getReleaseDate().compareTo(movie2.getReleaseDate());
        }

    }
}
