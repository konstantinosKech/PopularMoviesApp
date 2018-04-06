package com.udacity.kechagiaskonstantinos.popularmoviesapp.Utilities;

import android.util.Log;

import com.udacity.kechagiaskonstantinos.popularmoviesapp.dao.Movie;
import com.udacity.kechagiaskonstantinos.popularmoviesapp.dao.MovieVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by KechagiasKonstantinos on 26/02/2018.
 */

public class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();
    private static final String RESULT_TAG = "results";
    private static final String RELEASE_DATE_TAG = "release_date";
    private static final String ID_TAG = "id";
    private static final String POSTER_PATH_TAG = "poster_path";
    private static final String BACKDROP_PATH_TAG = "backdrop_path";
    private static final String TITLE_TAG = "title";
    private static final String VOTE_AVERAGE_TAG = "vote_average";
    private static final String OVERVIEW_TAG = "overview";
    private static final String KEY = "key";
    private static final String SITE = "site";
    private static final String TYPE = "type";

    private static final String IMAGES_TAG = "images";
    private static final String IMAGE_SIZE = "w185";
    private static final String SECURE_BASE_URL_TAG = "secure_base_url";

    public static final String DATE_FORMAT = "yyyy-MM-dd";


    public static ArrayList<MovieVideo> getMovieVideosFromJSON(String movieVideosResponse) {
        ArrayList<MovieVideo> returnArray = new ArrayList<MovieVideo>();

        try {
            JSONObject moviesTotal = new JSONObject(movieVideosResponse);
            JSONArray resultsArray = moviesTotal.getJSONArray(RESULT_TAG);
            for(int i = 0;i < resultsArray.length();i++){
                JSONObject resultObject=resultsArray.getJSONObject(i);
                MovieVideo movieVideo = new MovieVideo(resultObject.optString(KEY),resultObject.optString(SITE),resultObject.optString(TYPE));
                returnArray.add(movieVideo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"Cannot parse Json from movies");
        }

        return returnArray;
    }

        /**
         * Get a @{@link String} movieResponse JSON formatted and return an @{@link ArrayList} of @{@link Movie}
         *
         * @param moviesResponse
         * @return @{@link ArrayList} with all movies in moviesResponse @{@link String} JSON.
         */
    public static ArrayList<Movie> getMoviesFromJSON(String moviesResponse){
        ArrayList<Movie> returnArray = new ArrayList<Movie>();
        try {
            JSONObject moviesTotal = new JSONObject(moviesResponse);
            JSONArray resultsArray = moviesTotal.getJSONArray(RESULT_TAG);
            for(int i = 0;i < resultsArray.length();i++){
                JSONObject resultObject=resultsArray.getJSONObject(i);
                String stringDate = resultObject.optString(RELEASE_DATE_TAG);
                Date date;
                if(stringDate.isEmpty()){
                    date = null;
                }else {
                    SimpleDateFormat parser = new SimpleDateFormat(DATE_FORMAT);
                    date = parser.parse(stringDate);
                }
                Movie movie = new Movie(resultObject.optLong(ID_TAG), (!(resultObject.optString(POSTER_PATH_TAG).equals("null")))?MoviesDBNetworkUtils.buildImageUrl(resultObject.optString(POSTER_PATH_TAG)).toString():null, (!(resultObject.optString(BACKDROP_PATH_TAG).equals("null")))?MoviesDBNetworkUtils.buildImageUrl(resultObject.optString(BACKDROP_PATH_TAG)).toString():null, resultObject.optString(TITLE_TAG), date, resultObject.optDouble(VOTE_AVERAGE_TAG), resultObject.optString(OVERVIEW_TAG), MoviesDBNetworkUtils.getVideos(resultObject.optLong(ID_TAG)));
                returnArray.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"Cannot parse Json from movies");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG,"Cannot parse Json from movies");
        }
        return returnArray;
    }

    /**
     * Get a @{@link String} configurationResponse JSON formatted and return a @{@link String[] } with image API paths.
     *
     * @param configurationResponse
     * @return @{@link String[]}
     */
    public static String[] getImagePaths(String configurationResponse){
        try {
            JSONObject configurationTotal = new JSONObject(configurationResponse);
            JSONObject configurationImages = configurationTotal.getJSONObject(IMAGES_TAG);
            String secureBaseUrl = configurationImages.optString(SECURE_BASE_URL_TAG);
            //Log.v(TAG, "Json Parsed");
            return new String[]{secureBaseUrl,IMAGE_SIZE};
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"Cannot parse Json for images configuration urls");
        }
        return null;
    }
}
