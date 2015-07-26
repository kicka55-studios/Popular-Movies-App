package in.kicka55studios.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
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

public class MoviesService extends IntentService {

    public static final String SORT_ORDER_EXTRA = "soe";
    private final String LOG_TAG = MoviesService.class.getSimpleName();

    public MoviesService() {
        super("MoviesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String sortMethod = intent.getStringExtra(SORT_ORDER_EXTRA);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;
        int voteCountMin = 2000;
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
                return;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }

            moviesJsonStr = buffer.toString();
            getMoviesFromJson(moviesJsonStr);

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
        return;
    }

    private void getMoviesFromJson(String moviesJsonStr) {

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

//        String moviesList[] = null;

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

//            moviesList = new String[moviesArray.length()];

            Vector<ContentValues> cVector = new Vector<ContentValues>(moviesArray.length());

            for (int i = 0; i < moviesArray.length(); i++) {

                ContentValues values = new ContentValues();
                JSONObject movieObject = moviesArray.getJSONObject(i);

                String dateStr = movieObject.getString(TMDB_DATE);
                long movieId = movieObject.getInt(TMDB_MOVIE_ID);
                long votes = movieObject.getInt(TMDB_VOTES);
                String title = movieObject.getString(TMDB_TITLE);
                String overview = movieObject.getString(TMDB_OVERVIEW);
                double popularity = movieObject.getDouble(TMDB_POPULARITY);
                double rating = movieObject.getDouble(TMDB_RATING);
                String posterPath = movieObject.getString(TMDB_POSTER_PATH);

                values.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieId);
                values.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
                values.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                values.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, popularity);
                values.put(MoviesContract.MoviesEntry.COLUMN_RATING, rating);
                values.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, dateStr);
                values.put(MoviesContract.MoviesEntry.COLUMN_VOTES, votes);
                values.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, POSTER_PATH_PREFIX + POSTER_SIZE + posterPath + API_PARAM + MoviesContract.API_KEY);
//                moviesList[i] = movieId + " - " + title;

                cVector.add(values);
            }


            int inserted = 0;
            if (cVector.size() > 0) {
                ContentValues[] cArray = new ContentValues[cVector.size()];
                cVector.toArray(cArray);

                inserted = this.getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cArray);
            }
            Log.d(LOG_TAG, "Movies Service completed, " + inserted + " successful inserts.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
