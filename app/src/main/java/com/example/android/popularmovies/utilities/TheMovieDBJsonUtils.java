package com.example.android.popularmovies.utilities;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by thib146 on 20/01/2017.
 */

public class TheMovieDBJsonUtils {

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the movies with their respective information
     *
     * @param movieJsonStr JSON response from server
     *
     * @param posterVersion Poster size version, depending on the activity
     *
     * @return Array of Strings describing movie data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static MovieArrays getMovieDataFromJson(Context context, String movieJsonStr, String posterVersion)
            throws JSONException {

        // Page numbers
        final String TMDB_PAGE_NUMBER = "page";
        final String TMDB_TOTAL_PAGES = "total_pages";

        /* Movies information. Each movie info is an element of the "results" array */
        final String TMDB_LIST = "results";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_ID = "id";
        final String TMDB_TITLE = "title";
        final String TMDB_ORG_TITLE = "original_title";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_VOTE_COUNT = "vote_count";
        final String TMDB_VOTE_AVERAGE = "vote_average";

        final String TMDB_BASE_URL_POSTER = "http://image.tmdb.org/t/p/";
        final String TMDB_STATUS_CODE = "status_code";

        // Global Json object
        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Is there an error? */
        if (movieJson.has(TMDB_STATUS_CODE)) {
            int errorCode = movieJson.getInt(TMDB_STATUS_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        // Variable to store all the poster paths URLs
        URL[] urlPosterPath;

        // Json array
        JSONArray movieArray;

        //Movie parsedMovieData = new Movie();
        MovieArrays parsedMovieData = new MovieArrays();

        String totalPagesString;

        if (movieJson.has(TMDB_LIST)) { // If we're reading the results after a query
            totalPagesString = movieJson.getString(TMDB_TOTAL_PAGES);

            movieArray = movieJson.getJSONArray(TMDB_LIST);

            String currentPage = movieJson.getString(TMDB_PAGE_NUMBER);

            urlPosterPath = new URL[movieArray.length()];

            // Instantiate all the variables that we need
            ArrayList<String> movieTitle = new ArrayList<String>();
            ArrayList<String> posterPath = new ArrayList<String>();
            ArrayList<String> description = new ArrayList<String>();
            ArrayList<String> releaseDate = new ArrayList<String>();
            ArrayList<String> id = new ArrayList<String>();
            ArrayList<String> originalTitle = new ArrayList<String>();
            ArrayList<String> popularity = new ArrayList<String>();
            ArrayList<String> voteCount = new ArrayList<String>();
            ArrayList<String> voteAverage = new ArrayList<String>();

            for (int i = 0; i < movieArray.length(); i++) {

                    /* Get the JSON object representing one movie */
                JSONObject oneMovie = movieArray.getJSONObject(i);

                // Copy the read values to the corresponding variables
                movieTitle.add(oneMovie.getString(TMDB_TITLE));
                posterPath.add(oneMovie.getString(TMDB_POSTER_PATH));
                description.add(oneMovie.getString(TMDB_OVERVIEW));
                releaseDate.add(oneMovie.getString(TMDB_RELEASE_DATE));
                id.add(oneMovie.getString(TMDB_ID));
                originalTitle.add(oneMovie.getString(TMDB_ORG_TITLE));
                popularity.add(oneMovie.getString(TMDB_POPULARITY));
                voteCount.add(oneMovie.getString(TMDB_VOTE_COUNT));
                voteAverage.add(oneMovie.getString(TMDB_VOTE_AVERAGE));


                // Remove the first letter from the moviePoser string : the character "/" which is not useful
                String posterPathSubs = posterPath.get(i).substring(1);

                // Build the URI for the Poster Path
                Uri builtUri = Uri.parse(TMDB_BASE_URL_POSTER).buildUpon()
                        .appendPath(posterVersion)
                        .appendPath(posterPathSubs)
                        .build();

                // Create the Poster Path URL
                try {
                    urlPosterPath[i] = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                // Copy the corrected value to the posterPath variable
                posterPath.set(i, urlPosterPath[i].toString());
            }

            // Copy the full data in the parsedMovieData variable
            parsedMovieData.description = description;
            parsedMovieData.id = id;
            parsedMovieData.originalTitle = originalTitle;
            parsedMovieData.popularity = popularity;
            parsedMovieData.posterPath = posterPath;
            parsedMovieData.releaseDate = releaseDate;
            parsedMovieData.title = movieTitle;
            parsedMovieData.voteAverage = voteAverage;
            parsedMovieData.voteCount = voteCount;

            parsedMovieData.totalPageNumber = totalPagesString;

        } else { // If we're reading the info of one movie
            totalPagesString = "1"; // Set the total pages with a random value

            //Copy the read values to the corresponding variables
            String movieTitle = movieJson.getString(TMDB_TITLE);
            String posterPath = movieJson.getString(TMDB_POSTER_PATH);
            String description = movieJson.getString(TMDB_OVERVIEW);
            String releaseDate = movieJson.getString(TMDB_RELEASE_DATE);
            String id = movieJson.getString(TMDB_ID);
            String originalTitle = movieJson.getString(TMDB_ORG_TITLE);
            String popularity = movieJson.getString(TMDB_POPULARITY);
            String voteCount = movieJson.getString(TMDB_VOTE_COUNT);
            String voteAverage = movieJson.getString(TMDB_VOTE_AVERAGE);

            /**
             * Remove the first letter from the moviePoser string : the character "/" which is not useful
             */
            posterPath = posterPath.substring(1);

            urlPosterPath = new URL[movieJson.length()];

            // Build the URI for the Poster Path
            Uri builtUri = Uri.parse(TMDB_BASE_URL_POSTER).buildUpon()
                    .appendPath(posterVersion)
                    .appendPath(posterPath)
                    .build();

            // Build the Poster Path URL
            try {
                urlPosterPath[0] = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // Copy the built URL to the posterPath variable
            posterPath = urlPosterPath[0].toString();

            // Instantiate all the variables that we need
            parsedMovieData.posterPath = new ArrayList<String>();
            parsedMovieData.description = new ArrayList<String>();
            parsedMovieData.title = new ArrayList<String>();
            parsedMovieData.releaseDate = new ArrayList<String>();
            parsedMovieData.id = new ArrayList<String>();
            parsedMovieData.originalTitle = new ArrayList<String>();
            parsedMovieData.popularity = new ArrayList<String>();
            parsedMovieData.voteCount = new ArrayList<String>();
            parsedMovieData.voteAverage = new ArrayList<String>();

            // Copy the data in the parsedMovieData variable
            parsedMovieData.description.add(0, description);
            parsedMovieData.id.add(0, id);
            parsedMovieData.originalTitle.add(0, originalTitle);
            parsedMovieData.popularity.add(0, popularity);
            parsedMovieData.posterPath.add(0, posterPath);
            parsedMovieData.releaseDate.add(0, releaseDate);
            parsedMovieData.title.add(0, movieTitle);
            parsedMovieData.voteAverage.add(0, voteAverage);
            parsedMovieData.voteCount.add(0, voteCount);
            parsedMovieData.totalPageNumber = totalPagesString;
        }
        // Return the read data
        return parsedMovieData;
    }
}