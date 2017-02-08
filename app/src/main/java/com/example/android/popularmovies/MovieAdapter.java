package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.utilities.MovieArrays;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by thib146 on 20/01/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    // Global variable containing all the movies currently loaded
    // -- Arrays are used to be able to easily add more movies as the user scrolls down
    private MovieArrays mMoviesData;

    // Global int for the position of an item
    public int adapterPosition;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MovieAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(String movieDetails);
    }

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMovieImageView;
        public final TextView mMovieTitleTextView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMovieImageView = (ImageView) view.findViewById(R.id.iv_movie_data);
            mMovieTitleTextView = (TextView) view.findViewById(R.id.tv_movie_title_main);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            adapterPosition = getAdapterPosition();
            String movieDetails = mMoviesData.posterPath.get(adapterPosition);
            mClickHandler.onClick(movieDetails);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType In case of multiple view types
     * @return A new MovieAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param movieAdapterViewHolder    The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        String oneMoviePoster = mMoviesData.posterPath.get(position);
        String oneMovieTitle = mMoviesData.title.get(position);
        Context context = movieAdapterViewHolder.mMovieImageView.getContext();

        // Display the movie poster and the movie title in the RecyclerView
        Picasso.with(context).load(oneMoviePoster).into(movieAdapterViewHolder.mMovieImageView);
        movieAdapterViewHolder.mMovieTitleTextView.setText(oneMovieTitle);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mMoviesData) return 0;
        return mMoviesData.posterPath.size();
    }

    /**
     * This method is used to set the movie data on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param moviesData The new movie data to be displayed.
     */
    public void setMovieData(MovieArrays moviesData) {

        // Instantiate of all the variable that we need
        mMoviesData = new MovieArrays();
        mMoviesData.posterPath = new ArrayList<String>();
        mMoviesData.description = new ArrayList<String>();
        mMoviesData.title = new ArrayList<String>();
        mMoviesData.releaseDate = new ArrayList<String>();
        mMoviesData.id = new ArrayList<String>();
        mMoviesData.originalTitle = new ArrayList<String>();
        mMoviesData.popularity = new ArrayList<String>();
        mMoviesData.voteCount = new ArrayList<String>();
        mMoviesData.voteAverage = new ArrayList<String>();

        // Check if the data passed is null
        if (moviesData == null) {
            mMoviesData = null;
            return;
        }

        // Add the data passed (moviesData) to each member of mMovieData at the right position
        for (int i=0; i<20; i++) {
            mMoviesData.posterPath.add(i, moviesData.posterPath.get(i));
            mMoviesData.description.add(i, moviesData.description.get(i));
            mMoviesData.releaseDate.add(i, moviesData.releaseDate.get(i));
            mMoviesData.id.add(i, moviesData.id.get(i));
            mMoviesData.title.add(i, moviesData.title.get(i));
            mMoviesData.originalTitle.add(i, moviesData.originalTitle.get(i));
            mMoviesData.popularity.add(i, moviesData.popularity.get(i));
            mMoviesData.voteCount.add(i, moviesData.voteCount.get(i));
            mMoviesData.voteAverage.add(i, moviesData.voteAverage.get(i));
        }

        notifyDataSetChanged();
    }

    /**
     * This method is used to set a new list movies on the MovieAdapter when the user reaches the
     * bottom of the RecyclerView
     *
     * @param moviesData The new movie data to be displayed.
     */
    public void addMovieData(MovieArrays moviesData) {

        if (moviesData == null) {
            return;
        }
        int lengthMoviesData = mMoviesData.posterPath.size();

        for (int i = 0; i < 20; i++) {
            mMoviesData.posterPath.add(i+lengthMoviesData, moviesData.posterPath.get(i));
            mMoviesData.description.add(i+lengthMoviesData, moviesData.description.get(i));
            mMoviesData.releaseDate.add(i+lengthMoviesData, moviesData.releaseDate.get(i));
            mMoviesData.id.add(i+lengthMoviesData, moviesData.id.get(i));
            mMoviesData.title.add(i+lengthMoviesData, moviesData.title.get(i));
            mMoviesData.originalTitle.add(i+lengthMoviesData, moviesData.originalTitle.get(i));
            mMoviesData.popularity.add(i+lengthMoviesData, moviesData.popularity.get(i));
            mMoviesData.voteCount.add(i+lengthMoviesData, moviesData.voteCount.get(i));
            mMoviesData.voteAverage.add(i+lengthMoviesData, moviesData.voteAverage.get(i));
        }
        notifyDataSetChanged();
    }
}