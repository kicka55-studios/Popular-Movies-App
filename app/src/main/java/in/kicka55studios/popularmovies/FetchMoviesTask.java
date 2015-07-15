package in.kicka55studios.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import in.kicka55studios.popularmovies.data.MoviesContract;
import in.kicka55studios.popularmovies.data.MoviesContract.MoviesEntry;
import in.kicka55studios.popularmovies.data.MoviesDbHelper;

public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private final Context mContext;

    public FetchMoviesTask(Context mContext) {
        this.mContext = mContext;
    }


    private String[] getMoviesFromJson(String moviesJsonStr) {

        final String TMDB_RESULTS = "results";

        final String POSTER_PATH_PREFIX = "http://image.tmdb.org/t/p/";
        final String POSTER_SIZE = "w185";
        final String API_PARAM = "?api_key=";


        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_TITLE = "original_title";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_RATING = "vote_average";
        final String TMDB_VOTES = "vote_count";
        final String TMDB_DATE = "release_date";

        String moviesList[] = null;

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            moviesList = new String[moviesArray.length()];

            Vector<ContentValues> cVector = new Vector<ContentValues>(moviesArray.length());

            for (int i = 0; i < moviesArray.length(); i++) {

                ContentValues values = new ContentValues();
                JSONObject movieObject = moviesArray.getJSONObject(i);

                String dateStr = movieObject.getString(TMDB_DATE);
                int movieId = movieObject.getInt(TMDB_MOVIE_ID);
                int votes = movieObject.getInt(TMDB_VOTES);
                String title = movieObject.getString(TMDB_TITLE);
                String overview = movieObject.getString(TMDB_OVERVIEW);
                double popularity = movieObject.getDouble(TMDB_POPULARITY);
                double rating = movieObject.getDouble(TMDB_RATING);
                String posterPath = movieObject.getString(TMDB_POSTER_PATH);

                values.put(MoviesEntry.COLUMN_MOVIE_ID, movieId);
                values.put(MoviesEntry.COLUMN_TITLE, title);
                values.put(MoviesEntry.COLUMN_OVERVIEW, overview);
                values.put(MoviesEntry.COLUMN_POPULARITY, popularity);
                values.put(MoviesEntry.COLUMN_RATING, rating);
                values.put(MoviesEntry.COLUMN_RELEASE_DATE, dateStr);
                values.put(MoviesEntry.COLUMN_VOTES, votes);
                values.put(MoviesEntry.COLUMN_POSTER_PATH, POSTER_PATH_PREFIX + POSTER_SIZE + posterPath + API_PARAM + MoviesContract.API_KEY);
                moviesList[i] = movieId + " - " + title;

                cVector.add(values);
            }


            int returnCount = 0;
            if (cVector.size() > 0) {
                SQLiteDatabase db = new MoviesDbHelper(mContext).getWritableDatabase();
                db.beginTransaction();
                try {
                    ContentValues[] cArray = new ContentValues[cVector.size()];
                    cVector.toArray(cArray);
                    for (ContentValues value : cArray) {
                        long _id = db.insert(MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    Log.i(LOG_TAG, returnCount + " successful inserts.");
                }
            }

            return moviesList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String[] doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        String sortMethod = params[0];
        int voteCountMin = 1000;
        String apiKey = MoviesContract.API_KEY;

        try {
            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String API_PARAM = "api_key";
            final String VOTE_COUNT_PARAM = "vote_count.gte";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sortMethod)
                    .appendQueryParameter(VOTE_COUNT_PARAM, Integer.toString(voteCountMin))
                    .appendQueryParameter(API_PARAM, apiKey).build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            moviesJsonStr = buffer.toString();
            return getMoviesFromJson(moviesJsonStr);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);

        MoviesFragment.mMoviesAdapter.clear();
        for (String movie : strings) {
            MoviesFragment.mMoviesAdapter.add(movie);
        }

        MoviesFragment.listItems = strings;


    }
}
