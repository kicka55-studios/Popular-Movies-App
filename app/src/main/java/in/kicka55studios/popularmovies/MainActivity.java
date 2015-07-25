package in.kicka55studios.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    private final String MOVIESFRAGMENT_TAG = "MFTAG";
    private String mSortType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSortType = Utility.getPreferredSort(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container,
                    new MoviesFragment(),
                    MOVIESFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortOrder = Utility.getPreferredSort(this);

        if (!sortOrder.equals(mSortType)) {
            MoviesFragment mf = (MoviesFragment) getSupportFragmentManager().findFragmentByTag(MOVIESFRAGMENT_TAG);
            if (mf != null) {
                mf.onSortChanged();
            }
            mSortType = sortOrder;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
