package com.udacity.kechagiaskonstantinos.popularmoviesapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.kechagiaskonstantinos.popularmoviesapp.Utilities.MoviesDBNetworkUtils;
import com.udacity.kechagiaskonstantinos.popularmoviesapp.dao.Movie;
import com.udacity.kechagiaskonstantinos.popularmoviesapp.data.MoviesContract;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.ItemClickListener, LoaderManager.LoaderCallbacks<ArrayList<Movie>>{

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.rv_movies) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;

    private MoviesAdapter mMoviesAdapter;

    private String movieSort = POPULAR;
    public static final String POPULAR = "Popular";
    public static final String RATE = "Rate";
    public static final String FAVORITE = "Favorite";

    public final Integer GRID_SPACING = 10;

    private final int MOVIES_LOADER_ID = 100;

    @StringDef({POPULAR, RATE, FAVORITE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MovieSort {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        GridLayoutManager layoutManager
                = new GridLayoutManager(this,numberOfColumns());

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(GRID_SPACING));
        mMoviesAdapter = new MoviesAdapter(this,this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        getSupportLoaderManager().initLoader(MOVIES_LOADER_ID,null,MainActivity.this);
    }

    /**
     * This function return the number of the column of Grid Layout Manager
     *
     * @return @{@link Integer}
     */
    private Integer numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    private void loadMovieData() {
        showMovieDataView();
        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, MainActivity.this);

    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(Movie movie) {
        //Log.i(TAG,movie.getMovieId().toString());
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("Movie", movie);
        startActivity(intentToStartDetailActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.moviesort_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_popular) {
            mMoviesAdapter.setMoviesData(null);
            movieSort = POPULAR;
            loadMovieData();
            return true;
        } else if (id == R.id.sort_rate) {
            mMoviesAdapter.setMoviesData(null);
            movieSort = RATE;
            loadMovieData();
            return true;
        } else if (id == R.id.sort_favorite) {
            mMoviesAdapter.setMoviesData(null);
            movieSort = FAVORITE;
            loadMovieData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<ArrayList<Movie>>(this){

            ArrayList<Movie> movies = null;

            @Override
            protected void onStartLoading() {
                if (movies != null) {
                    movies = null;
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }
            @Override
            public ArrayList<Movie> loadInBackground() {

                ArrayList<Movie> mMovies;
                if(movieSort.equals(POPULAR))
                    mMovies = MoviesDBNetworkUtils.getMovies(POPULAR,getContentResolver());
                else if(movieSort.equals(RATE))
                    mMovies = MoviesDBNetworkUtils.getMovies(RATE,getContentResolver());
                else{
                    ArrayList<Movie> movieArrayList = new ArrayList<Movie>();
                    Cursor c = getContentResolver().query(MoviesContract.FavoriteMoviesEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            MoviesContract.FavoriteMoviesEntry.COLUMN_FAVORITE_MOVIE_ID);
                    if(c == null){
                        Log.e(TAG,"Cursor is null");
                        return null;
                    }
                    if(c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            movieArrayList.add(new Movie(c.getLong(0)));
                        } while (c.moveToNext());
                    }
                    c.close();
                    mMovies = MoviesDBNetworkUtils.getMoviesFromMovieList(movieArrayList);
                }
                return mMovies;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<ArrayList<Movie>> loader, ArrayList<Movie> movies) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        if (movies != null) {
            showMovieDataView();
            mMoviesAdapter.setMoviesData(movies.toArray(new Movie[0]));
        }else
            showErrorMessage();
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<ArrayList<Movie>> loader) {

    }

    /**
     * This class is to Decorate the space between GridLayoutManager components
     */
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }
}
