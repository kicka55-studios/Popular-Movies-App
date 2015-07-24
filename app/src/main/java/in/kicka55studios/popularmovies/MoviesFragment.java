package in.kicka55studios.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import in.kicka55studios.popularmovies.data.MoviesContract;

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity());
        String sort = Utility.getPreferredSort(getActivity());
        moviesTask.execute(sort);
    }


//    public void getData() {
//
//        String sortPref = Utility.getPreferredSort(getActivity());
//
//        Cursor c = null;
//        Uri uri = null;
//
//        if (sortPref.equals(getString(R.string.pref_sort_popularity))) {
//            uri = MoviesContract.BASE_CONTENT_URI.buildUpon().appendPath(MoviesContract.PATH_POPULARITY).build();
//        } else {
//            uri = MoviesContract.BASE_CONTENT_URI.buildUpon().appendPath(MoviesContract.PATH_RATING).build();
//        }
//        c = getActivity().getContentResolver().query(uri, null, null, null, null);
//
//        mMoviesAdapter = new MoviesAdapter(getActivity(), c, 0);
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortPref = Utility.getPreferredSort(getActivity());

        Uri uri = null;

        if (sortPref.equals(getString(R.string.pref_sort_popularity))) {
            uri = MoviesContract.MoviesEntry.CONTENT_URI.buildUpon().appendPath(MoviesContract.POPULARITY).build();
        } else {
            uri = MoviesContract.MoviesEntry.CONTENT_URI.buildUpon().appendPath(MoviesContract.RATING).build();
        }
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMoviesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }
}
