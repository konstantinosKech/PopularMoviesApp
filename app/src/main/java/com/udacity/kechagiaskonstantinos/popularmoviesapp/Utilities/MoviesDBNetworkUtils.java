package com.udacity.kechagiaskonstantinos.popularmoviesapp.Utilities;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import com.udacity.kechagiaskonstantinos.popularmoviesapp.MainActivity;
import com.udacity.kechagiaskonstantinos.popularmoviesapp.dao.Movie;
import com.udacity.kechagiaskonstantinos.popularmoviesapp.dao.MovieReview;
import com.udacity.kechagiaskonstantinos.popularmoviesapp.dao.MovieVideo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import static com.udacity.kechagiaskonstantinos.popularmoviesapp.MainActivity.POPULAR;

/**
 * Created by kechagiaskonstantinos on 26/02/2018.
 */

public class MoviesDBNetworkUtils {
    private static final String TAG = MoviesDBNetworkUtils.class.getSimpleName();

    private static final String MOVIE_POPULAR_URL = "https://api.themoviedb.org/3/movie/popular";
    private static final String MOVIE_TOP_RATED_URL = "https://api.themoviedb.org/3/movie/top_rated";
    private static final String MOVIE_DB_URL = "https://api.themoviedb.org/3/";
    private static final String SINGLE_MOVIE_DB_URL = MOVIE_DB_URL + "movie/%s";
    private static final String MOVIE_VIDEO_URL = MOVIE_DB_URL + "movie/%s/videos";
    private static final String MOVIE_REVIEW_URL = MOVIE_DB_URL + "movie/%s/reviews";
    private static final String CONFIGURATION_SUB_URL = "configuration";

    private static final String API_PARAM = "api_key";
    private static final String API_KEY = "eb68acebf9644349b99549ab101d948c";
    private static final String LANG_PARAM = "language";
    private static final String ENG_LANG_VALUE = "en-US";
    private static final String PAGE_PARAM = "page";
    private static final String PAGE_VALUE = "1";

    private static String[] imagesUrls = null;

    /**
     * Get the id of the movie and return an @{@link ArrayList} of @{@link MovieVideo}
     *
     * @param moviedId
     * @return @{@link ArrayList} of @{@link MovieVideo}
     */
    public static ArrayList<MovieVideo> getVideos(Long moviedId){
        try {
            String movieVideoResponse = getResponseFromHttpUrl(buildMovieVideosUrl(moviedId));
            return JsonUtils.getMovieVideosFromJSON(movieVideoResponse);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Cannot get fucking movie videos");
        }
        return null;
    }

    /**
     *
     * Get the id of the movie and return an @{@link ArrayList} of @{@link MovieReview}
     *
     * @param moviedId
     * @return @{@link ArrayList} of @{@link MovieReview}
     */
    public static ArrayList<MovieReview> getReviews(Long moviedId){
        try {
            String movieVideoResponse = getResponseFromHttpUrl(buildMovieReviewUrl(moviedId));
            return JsonUtils.getMovieReviewsFromJSON(movieVideoResponse);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Cannot get fucking movie reviews");
        }
        return null;
    }

    /**
     * Get a @{@link com.udacity.kechagiaskonstantinos.popularmoviesapp.MainActivity.MovieSort} option and return the movies.
     *
     * @param movieSort
     * @return @{@link ArrayList} of @{@link Movie}
     */
    public static ArrayList<Movie> getMovies(@MainActivity.MovieSort String movieSort, ContentResolver contentResolver){
        try {
            String movieResponse = getResponseFromHttpUrl(buildMovieUrl(movieSort));
            return JsonUtils.getMoviesFromJSON(movieResponse);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Cannot get fucking movies");
        }
        return null;
    }

    /**
     *
     * Get Movie details for all Movies in the @{@link ArrayList} of @{@link Movie}
     *
     * @param movieArrayList
     * @return @{@link ArrayList} of @{@link Movie}
     */
    public static ArrayList<Movie> getMoviesFromMovieList(ArrayList<Movie> movieArrayList){
        ArrayList<String> jsonList = new ArrayList<String>();
        for(Movie movie : movieArrayList){
            try {
                jsonList.add(getResponseFromHttpUrl(buildSingleMovieUrl(movie.getMovieId())));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"Cannot get fucking movie : " + movie.getMovieId());
            }
        }
        return JsonUtils.getMoviesFromMoviesJsonArray(jsonList);
    }

    /**
     * Get a @{@link com.udacity.kechagiaskonstantinos.popularmoviesapp.MainActivity.MovieSort} option and return the @{@link URL} with the movies
     *
     * @param movieSort
     * @return @{@link URL} of movies
     */
    private static URL buildMovieUrl(@MainActivity.MovieSort String movieSort){
        Uri builtUri;
        if(movieSort.equals(POPULAR))
            builtUri = Uri.parse(MOVIE_POPULAR_URL).buildUpon()
                .appendQueryParameter(API_PARAM,API_KEY)
                .appendQueryParameter(LANG_PARAM,ENG_LANG_VALUE)
                .appendQueryParameter(PAGE_PARAM,PAGE_VALUE)
                .build();
        else
            builtUri = Uri.parse(MOVIE_TOP_RATED_URL).buildUpon()
                    .appendQueryParameter(API_PARAM,API_KEY)
                    .appendQueryParameter(LANG_PARAM,ENG_LANG_VALUE)
                    .appendQueryParameter(PAGE_PARAM,PAGE_VALUE)
                    .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG,"Unable to build Movie URL");
        }
        return url;
    }

    /**
     * Get movie id and generate the @{@link URL} for the movie details
     *
     * @param movieId
     * @return @{@link URL}
     */
    private static URL buildSingleMovieUrl(Long movieId){
        String videoUrl = String.format(SINGLE_MOVIE_DB_URL, String.valueOf(movieId));
        Uri builtUri;
        builtUri = Uri.parse(videoUrl).buildUpon()
                .appendQueryParameter(API_PARAM,API_KEY)
                .build();
        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG,"Unable to build single movie URL");
        }
        return url;
    }

    /**
     * Get movie id and generate the @{@link URL} for the movie videos
     *
     * @param movieId
     * @return @{@link URL}
     */
    private static URL buildMovieVideosUrl(Long movieId){
        String videoUrl = String.format(MOVIE_VIDEO_URL, String.valueOf(movieId));
        Uri builtUri;
        builtUri = Uri.parse(videoUrl).buildUpon()
                .appendQueryParameter(API_PARAM,API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG,"Unable to build Movie Videos URL");
        }
        return url;
    }

    /**
     * Get movie id and generate the @{@link URL} for the movie reviews
     *
     * @param movieId
     * @return @{@link URL}
     */
    private static URL buildMovieReviewUrl(Long movieId){
        String videoUrl = String.format(MOVIE_REVIEW_URL, String.valueOf(movieId));
        Uri builtUri;
        builtUri = Uri.parse(videoUrl).buildUpon()
                .appendQueryParameter(API_PARAM,API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG,"Unable to build Movie Review URL");
        }
        return url;
    }

    /**
     * Get the imagePath as @{@link String} and return the @{@link URL} of the image.
     *
     * @param imagePath
     * @return @{@link URL} of the image.
     */
    public static URL buildImageUrl(String imagePath){
        try {
            //To load imageUrls only one time
            if (imagesUrls == null){
                String configurationResponse = getResponseFromHttpUrl(buildConfigurationUrl());
                imagesUrls = JsonUtils.getImagePaths(configurationResponse);
                if ((imagesUrls == null) || imagesUrls.length != 2)
                    return null;
            }

            Uri builtUri = Uri.parse(imagesUrls[0] + imagesUrls[1] + imagePath).buildUpon()
                    .build();

            URL url = null;
            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG,"Unable to build image URL");
            }
            //Log.v(TAG, "Built URI for Images " + url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Unable to build image URL");
        }
        return null;
    }

    /**
     *This method return the configuration @{@link URL}
     *
     * @return
     */
    private static URL buildConfigurationUrl(){
        Uri builtUri = Uri.parse(MOVIE_DB_URL + CONFIGURATION_SUB_URL).buildUpon()
                .appendQueryParameter(API_PARAM, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG,"Unable to build configuration URL");
        }
        //Log.v(TAG, "Built URI for configuration" + url);
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    private static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        }finally {
            urlConnection.disconnect();
        }
    }
}
