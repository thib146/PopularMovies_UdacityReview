package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.MovieAdapter.MovieAdapterOnClickHandler;
import com.example.android.popularmovies.utilities.MovieArrays;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.TheMovieDBJsonUtils;
import com.example.android.popularmovies.widget.SegmentedButton;

import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popularmovies.utilities.NetworkUtils.isNetworkAvailable;

public class MainActivity extends AppCompatActivity
        implements MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    // RecyclerView + Adapter declarations
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;

    // Error Message TextView
    private TextView mErrorMessageDisplay;

    // ProgressBar
    private ProgressBar mLoadingIndicator;

    // String storing the sort query, set by default to "popular"
    private String sortQuery = "popular";

    // Array containing all the movie IDs, used to know which item we clicked on
    private ArrayList<String> mMovieId;

    // MovieArray containing all the movies currently loaded
    private MovieArrays[] mMovie;

    // Two different poster sizes, depending on the device orientation
    private String mPosterPortraitVersion = "w500";
    private String mPosterLandscapeVersion = "w342";

    // Number of columns on the grid in the RecyclerView
    private int numColumns = 2;

    // Variables to store the current page number and the total number of pages
    private String mCurrentPageNumber = "1"; // Initialization to 1
    private String mTotalPageNumber;

    // Boolean to store the internet connection status
    private static boolean mConnected = true;

    // Boolean to know add more data when we reach the bottom ONLY ONCE
    private boolean mBottomReachedOnce = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Creation of the Segmented Buttons using Widget/SegmentedButton
         */
        SegmentedButton buttons = (SegmentedButton)findViewById(R.id.segmented);
        buttons.clearButtons();
        buttons.addButtons(getString(R.string.popular_button), getString(R.string.ratings_button));
        // First button is selected
        buttons.setPushedButtonIndex(0);
        // Some example click handlers. Note the click won't get executed
        // if the segmented button is already selected (dark blue)
        buttons.setOnClickListener(new SegmentedButton.OnClickListenerSegmentedButton() {
            @Override
            public void onClick(int index) {
                if (index == 0) {
                    sortQuery = "popular";
                    mMovieAdapter.setMovieData(null);   // Clean the movie data
                    mCurrentPageNumber = "1";           // Reset the current page number
                    loadMovieData();                    // Reload the movie data
                } else {
                    sortQuery = "top_rated";
                    mMovieAdapter.setMovieData(null);
                    mCurrentPageNumber = "1";
                    loadMovieData();
                }
            }
        });

        /**
         * Management of menu buttons
         */
        // REFRESH BUTTON
        ImageView reload = (ImageView) findViewById(R.id.iv_refresh_menu);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMovieAdapter.setMovieData(null);   // Clean the movie data
                mCurrentPageNumber = "1";           // Reset the current page number
                loadMovieData();                    // Reload the movie data
            }
        });
        // SETTINGS BUTTON
        final ImageView settings = (ImageView) findViewById(R.id.iv_settings_menu);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToStartSettingsActivity = new Intent(MainActivity.this, MainSettings.class); // Launch Settings Activity
                startActivity(intentToStartSettingsActivity);
            }
        });

        // RecyclerView reference
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        // Error message reference
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * GridLayoutManager declaration for our movie posters. Limited to 2 columns
         * A custom GridLayoutManager is used to prevent a bug explained in the class
         */
        final CustomGridLayoutManager layoutManager
                = new CustomGridLayoutManager(this, numColumns, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

        // OnScrollListener added to the RecyclerView to know when the user reached the bottom
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                if (pastVisibleItems + visibleItemCount == totalItemCount && !mBottomReachedOnce) { // If we reach the bottom
                    Log.e(TAG, "Bottom reached!");  // Log the event
                    loadMoreMovieData();            // Load more data (20 more movies)
                    mBottomReachedOnce = true;      // Block the access for 1 second, to prevent the load to happen 10 times
                }
            }
        });

        /*
         * The MovieAdapter is responsible for linking our movie data with the Views that
         * will end up displaying our movie data.
         */
        mMovieAdapter = new MovieAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mMovieAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the movie data. */
        loadMovieData();
    }

    private static class CustomGridLayoutManager extends GridLayoutManager {
        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public CustomGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public CustomGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public CustomGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }
    }

    /**
     * This method will get sort query from the user, and then tell some
     * background method to get the movie data in the background.
     */
    private void loadMovieData() {
        showMovieDataView();

        new FetchMovieTask().execute(sortQuery);
    }

    /**
     * This method will get sort query from the user, and then tell some
     * background method to get more movie data in the background.
     */
    private void loadMoreMovieData() {
        showMovieDataView();

        new FetchMoreMovieTask().execute(sortQuery);
    }

    /**
     * This method will change mConnected according to the internet connection
     */
    private static Handler connectionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != 1) { // If not connected
                mConnected = false;
            } else { // If connected
                mConnected = true;
            }
        }
    };

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param movieID The view that was clicked
     */
    @Override
    public void onClick(String movieID) {
        int adapterPosition = mMovieAdapter.adapterPosition;
        String movieId = mMovieId.get(adapterPosition);

        // Open the Detail Movie activity
        Intent intentToStartMovieDetailActivity = new Intent(this, MovieDetails.class);

        // Add the movie ID in the Extra
        intentToStartMovieDetailActivity.putExtra(Intent.EXTRA_TEXT, movieId);

        // Only start the activity if the internet connexion is on
        if (mConnected) {
            startActivity(intentToStartMovieDetailActivity);
        } else {
            showErrorMessage();
        }
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message.
     */
    private void showMovieDataView() {
        /* Hide the error message */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Make the movie data visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie
     * View.
     */
    private void showErrorMessage() {
        /* Hide the current data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Chose which error message to display */
        if (!mConnected) { // If the internet connexion is lost
            mErrorMessageDisplay.setText(R.string.error_message_internet);
        } else if (!NetworkUtils.isApiKeyOn()) { // If the API Key is empty
            mErrorMessageDisplay.setText(R.string.error_message_api_key);
        } else { // For any other problem
            mErrorMessageDisplay.setText(R.string.error_message_common);
        }
        /* Show the error view */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    // This method will load the 20 first movies in the background and send them to the Adapter
    public class FetchMovieTask extends AsyncTask<String, Void, MovieArrays[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        protected MovieArrays[] doInBackground(String... params) {

            /* If there's no data, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            // If there's no internet connexion, stop
            isNetworkAvailable(connectionHandler, 5000);
            if (!mConnected) {
                return null;
            }

            // If the API Key is empty, stop
            if (!NetworkUtils.isApiKeyOn()) {
                return null;
            }

            // Create the URL with the currentPageNumber
            URL movieRequestUrl = NetworkUtils.buildUrl(sortQuery, mCurrentPageNumber);

            try {
                // Get the full HTTP response
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                // Adapt the poster size, according to the device orientation
                String mPosterVersion;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mPosterVersion = mPosterLandscapeVersion;
                } else {
                    mPosterVersion = mPosterPortraitVersion;
                }

                // Read the 20 first movies from the Json file
                MovieArrays JsonMovieData = TheMovieDBJsonUtils
                        .getMovieDataFromJson(MainActivity.this, jsonMovieResponse, mPosterVersion);

                // Initialize the total page number
                mTotalPageNumber = JsonMovieData.totalPageNumber;
                int totalPageNumberInt = Integer.valueOf(mTotalPageNumber);

                // Instantiate all the variables that we need
                mMovie = new MovieArrays[totalPageNumberInt];

                for (int i = 0; i < totalPageNumberInt; i++) {
                    mMovie[i] = new MovieArrays();
                    mMovie[i].posterPath = new ArrayList<String>();
                    mMovie[i].title = new ArrayList<String>();
                    mMovie[i].id = new ArrayList<String>();
                    mMovie[i].description = new ArrayList<String>();
                    mMovie[i].releaseDate = new ArrayList<String>();
                    mMovie[i].originalTitle = new ArrayList<String>();
                    mMovie[i].popularity = new ArrayList<String>();
                    mMovie[i].voteCount = new ArrayList<String>();
                    mMovie[i].voteAverage = new ArrayList<String>();
                }

                // Copy the data from the Json to our global mMovie variable
                mMovie[0].posterPath = JsonMovieData.posterPath;
                mMovie[0].title = JsonMovieData.title;
                mMovie[0].id = JsonMovieData.id;
                mMovie[0].description = JsonMovieData.description;
                mMovie[0].releaseDate = JsonMovieData.releaseDate;
                mMovie[0].originalTitle = JsonMovieData.originalTitle;
                mMovie[0].popularity = JsonMovieData.popularity;
                mMovie[0].voteCount = JsonMovieData.voteCount;
                mMovie[0].voteAverage = JsonMovieData.voteAverage;

                // Copy the ID data from the Json to the global mMovieId variable
                mMovieId = mMovie[0].id;

                // Add 1 to the mCurrentPageNumber
                mCurrentPageNumber = "2";

                // Return the global mMovie data variable for it to be used in onPostExecute
                return mMovie;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MovieArrays[] movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) { // If the movie data has been read
                showMovieDataView();
                mMovieAdapter.setMovieData(movieData[0]); // Send the data to the Adapter

                // Once everything is loaded, allow the scroll feature again after 1 second
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        mBottomReachedOnce = false;
                    }
                }, 1000);
            } else {
                showErrorMessage();
            }
        }
    }

    // This method will get 20 more movies in the background and send them to the Adapter
    public class FetchMoreMovieTask extends AsyncTask<String, Void, MovieArrays[]> {

        protected MovieArrays[] doInBackground(String... params) {

            /* If there's no data, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            // Check the internet connection and stop everything if we lost it
            isNetworkAvailable(connectionHandler, 5000);
            if (!mConnected) {
                return null;
            }

            // Initialize the current and total page number
            int currentPageNumberInt = Integer.valueOf(mCurrentPageNumber);
            int totalPageNumberInt = Integer.valueOf(mTotalPageNumber);

            // If we reached the last page, stop
            if (currentPageNumberInt > totalPageNumberInt) {
                return null;
            }

            // Make the new URL for the next page of data
            URL movieRequestUrl = NetworkUtils.buildUrl(sortQuery, mCurrentPageNumber);

            try {
                // Get the full HTTP response
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieRequestUrl);

                // Adapt the poster size, according to the device orientation
                String mPosterVersion;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mPosterVersion = mPosterLandscapeVersion;
                } else {
                    mPosterVersion = mPosterPortraitVersion;
                }

                // Read 20 new movies from the Json file
                MovieArrays JsonMovieData = TheMovieDBJsonUtils
                        .getMovieDataFromJson(MainActivity.this, jsonMovieResponse, mPosterVersion);

                // Copy the new data from the Json to our global mMovie variable
                mMovie[currentPageNumberInt-1].posterPath = JsonMovieData.posterPath;
                mMovie[currentPageNumberInt-1].title = JsonMovieData.title;
                mMovie[currentPageNumberInt-1].id = JsonMovieData.id;
                mMovie[currentPageNumberInt-1].description = JsonMovieData.description;
                mMovie[currentPageNumberInt-1].releaseDate = JsonMovieData.releaseDate;
                mMovie[currentPageNumberInt-1].originalTitle = JsonMovieData.originalTitle;
                mMovie[currentPageNumberInt-1].popularity = JsonMovieData.popularity;
                mMovie[currentPageNumberInt-1].voteCount = JsonMovieData.voteCount;
                mMovie[currentPageNumberInt-1].voteAverage = JsonMovieData.voteAverage;

                // Add all the new IDs (20) to the mMovieId global variable
                for (int i = 0; i < 20; i++) {
                    mMovieId.add(mMovie[currentPageNumberInt-1].id.get(i));
                }

                // return the new data to use it in onPostExecute
                return mMovie;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MovieArrays[] movieData) {
            if (movieData != null) {
                int currentPageNumberInt = Integer.valueOf(mCurrentPageNumber);

                // Send the 20 new movies to the Adapter
                mMovieAdapter.addMovieData(movieData[currentPageNumberInt-1]);

                // Increase the currentPageNumber
                currentPageNumberInt += 1;
                mCurrentPageNumber = String.valueOf(currentPageNumberInt);

                // Once everything is loaded, allow the scroll feature again after 1 second
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        mBottomReachedOnce = false;
                    }
                }, 1000);
            } else {
                showErrorMessage();
            }
        }
    }
}