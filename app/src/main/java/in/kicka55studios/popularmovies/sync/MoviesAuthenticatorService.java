package in.kicka55studios.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Chirag Khurana on 27-Jul-15.
 */
public class MoviesAuthenticatorService extends Service {
    private MoviesAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new MoviesAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
