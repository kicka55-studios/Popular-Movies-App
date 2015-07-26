package in.kicka55studios.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import in.kicka55studios.popularmovies.data.MoviesContract;
import in.kicka55studios.popularmovies.service.MoviesService;

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_POSTER = 3;
    private static final String[] MOVIES_COLUMNS = {
            MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH
    };
    private static final int MOVIES_LOADER = 0;

    public static MoviesAdapter mMoviesAdapter;
    private ListView movieListView;

    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMoviesAdapter = new MoviesAdapter(getActivity(), null, 0);

        movieListView = (ListView) rootView.findViewById(R.id.movies_list_view);
        movieListView.setAdapter(mMoviesAdapter);

        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), Details.class).setData(MoviesContract.MoviesEntry.buildMovieWithId(cursor.getLong(COL_MOVIE_ID)));
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateMovies() {
        Intent intent = new Intent(getActivity(), MoviesService.class);
        intent.putExtra(MoviesService.SORT_ORDER_EXTRA, Utility.getPreferredSort(getActivity()));
        getActivity().startService(intent);
    }

    public void onSortChanged() {
        updateMovies();
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortPref = Utility.getPreferredSort(getActivity());

        Uri uri = null;

        if (sortPref.equals(getString(R.string.pref_sort_popularity))) {
            uri = MoviesContract.MoviesEntry.CONTENT_URI.buildUpon().appendPath(MoviesContract.POPULARITY).build();
        } else {
            uri = MoviesContract.MoviesEntry.CONTENT_URI.buildUpon().appendPath(MoviesContract.RATING).build();
        }
        return new CursorLoader(getActivity(), uri, MOVIES_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMoviesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
