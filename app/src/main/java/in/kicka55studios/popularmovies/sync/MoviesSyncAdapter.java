package in.kicka55studios.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import in.kicka55studios.popularmovies.R;
import in.kicka55studios.popularmovies.Utility;
import in.kicka55studios.popularmovies.data.MoviesContract;

public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting Sync");
        String sortMethod = Utility.getPreferredSort(getContext());

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

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

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

                cVector.add(values);
            }


            int inserted = 0;
            if (cVector.size() > 0) {
                ContentValues[] cArray = new ContentValues[cVector.size()];
                cVector.toArray(cArray);

                inserted = getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cArray);
            }
            Log.d(LOG_TAG, "Movies Sync completed, " + inserted + " successful inserts.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
