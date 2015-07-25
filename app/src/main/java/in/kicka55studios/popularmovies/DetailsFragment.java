package in.kicka55studios.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import in.kicka55studios.popularmovies.data.MoviesContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_RATING,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH
    };

    private static final int COL_ID = 0;
    private static final int COL_MOVIE_ID = 1;
    private static final int COL_MOVIE_TITLE = 2;
    private static final int COL_MOVIE_OVERVIEW = 3;
    private static final int COL_MOVIE_RATING = 4;
    private static final int COL_MOVIE_POSTER = 5;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                intent.getData(),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) {
            return;
        }

        ImageView movie_poster = (ImageView) getView().findViewById(R.id.movie_poster);
        TextView movie_title = (TextView) getView().findViewById(R.id.movie_title);
        TextView movie_overview = (TextView) getView().findViewById(R.id.movie_overview);
        TextView movie_rating = (TextView) getView().findViewById(R.id.movie_rating);

        String poster_url = data.getString(COL_MOVIE_POSTER);
        String title = data.getString(COL_MOVIE_TITLE);
        String overview = data.getString(COL_MOVIE_OVERVIEW);
        double rating = data.getDouble(COL_MOVIE_RATING);

        movie_title.setText(title);
        movie_overview.setText(overview);
        movie_rating.setText("Rating: " + String.valueOf(rating));
        Picasso.with(getActivity()).load(poster_url).into(movie_poster);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
