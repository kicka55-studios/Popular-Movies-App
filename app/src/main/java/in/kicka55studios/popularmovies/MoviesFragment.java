package in.kicka55studios.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.kicka55studios.popularmovies.data.MoviesContract;
import in.kicka55studios.popularmovies.data.MoviesDbHelper;

public class MoviesFragment extends Fragment {

    public static ArrayAdapter<String> mMoviesAdapter;
    public static String[] listItems;
    List<String> list = null;
    private ListView movieListView;


    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] listItems = {
                "Alpha",
                "Beta",
                "Gamma",
        };


        List<String> weekForecast = new ArrayList<String>(Arrays.asList(listItems));

        mMoviesAdapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.list_item_movies,
                        R.id.movies_list_item,
                        weekForecast);


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.movies_list_view);
        listView.setAdapter(mMoviesAdapter);
        getData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String txt = (String) parent.getItemAtPosition(position);
                String segments[] = txt.split("-");
                startActivity(new Intent(getActivity(), Details.class).putExtra(Intent.EXTRA_TEXT, segments[0].trim()));
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();

        String pref_sort = Utility.getPreferredSort(getActivity());
        new FetchMoviesTask(getActivity()).execute(pref_sort);
    }


    public void getData() {

        String sortPref = Utility.getPreferredSort(getActivity());

        Cursor c = null;

        if (sortPref.equals(getString(R.string.pref_sort_popularity))) {
            c = new MoviesDbHelper(getActivity()).getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                    null, null, null, null, null, MoviesContract.MoviesEntry.COLUMN_POPULARITY + " DESC");
        } else {
            c = new MoviesDbHelper(getActivity()).getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                    null, MoviesContract.MoviesEntry.COLUMN_VOTES + " > ?", new String[]{"1000"}, null, null, MoviesContract.MoviesEntry.COLUMN_RATING + " DESC");
        }


        int INDEX_ID = c.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID);
        int INDEX_TITLE = c.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE);
        int i = 0;
        mMoviesAdapter.clear();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            mMoviesAdapter.add(c.getInt(INDEX_ID) + " - " + c.getString(INDEX_TITLE));
            c.moveToNext();
            i++;
        }
    }
}
