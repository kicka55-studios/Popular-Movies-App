package in.kicka55studios.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import in.kicka55studios.popularmovies.data.MoviesContract;
import in.kicka55studios.popularmovies.data.MoviesDbHelper;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    TextView movie_title, movie_overview, movie_rating;
    ImageView movie_poster;
    String movieId;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        movie_poster = (ImageView) rootView.findViewById(R.id.movie_poster);
        movie_title = (TextView) rootView.findViewById(R.id.movie_title);
        movie_overview = (TextView) rootView.findViewById(R.id.movie_overview);
        movie_rating = (TextView) rootView.findViewById(R.id.movie_rating);

        movieId = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);

        SQLiteDatabase db = new MoviesDbHelper(getActivity()).getWritableDatabase();
        Cursor c = db.query(MoviesContract.MoviesEntry.TABLE_NAME,
                null,
                MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movieId},
                null,
                null,
                null);

        c.moveToFirst();
        String url = c.getString(c.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH));
        String title = c.getString(c.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE));
        String overview = c.getString(c.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OVERVIEW));
        double rating = c.getDouble(c.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RATING));

        movie_title.setText(title);
        movie_overview.setText(overview);
        movie_rating.setText("Rating: " + String.valueOf(rating));
        Picasso.with(getActivity()).load(url).into(movie_poster);

        return rootView;
    }
}
