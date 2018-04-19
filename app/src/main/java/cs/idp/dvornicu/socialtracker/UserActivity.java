package cs.idp.dvornicu.socialtracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.app.FragmentTransaction;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import cs.idp.dvornicu.socialtracker.fragments.UserActivityFragment;
import cs.idp.dvornicu.socialtracker.fragments.UserChatFragment;
import cs.idp.dvornicu.socialtracker.fragments.UserInfoFragment;
import cs.idp.dvornicu.socialtracker.services.LocationService;
import cs.idp.dvornicu.socialtracker.utils.DbEntry;
import cs.idp.dvornicu.socialtracker.utils.Util;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 9;

    public static JSONArray facebookFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        loadUserInfoFragment();

        mDrawerList = findViewById(R.id.navList);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        addDrawerItems();
        setupDrawer();
        getFacebookFriends();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        else {
            Intent serviceIntent = new Intent(this, LocationService.class);
            stopService(serviceIntent);
            startService(serviceIntent);
        }
    }

    private void loadUserInfoFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.user_fragment, new UserInfoFragment(), "FR_USERINFO");
        ft.commit();
    }

    private void loadUserActivityFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.user_fragment, new UserActivityFragment(), "FR_USERACTIVITY");
        ft.commit();
    }

    private void loadUserChatFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.user_fragment, new UserChatFragment(), "FR_USERCHAT");
        ft.commit();
    }

    private void addDrawerItems() {
        final String[] navOptions = { "My Page", "Activity", "Chat" };
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, navOptions);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mDrawerLayout != null) mDrawerLayout.closeDrawer(Gravity.START);

                switch (position) {
                    case 0:
                        UserInfoFragment userInfoFragment = (UserInfoFragment) getFragmentManager().findFragmentByTag("FR_USERINFO");
                        if (userInfoFragment != null && userInfoFragment.isVisible())
                            break;

                        loadUserInfoFragment();
                        break;
                    case 1:
                        UserActivityFragment userActivityFragment = (UserActivityFragment) getFragmentManager().findFragmentByTag("FR_USERACTIVITY");
                        if (userActivityFragment != null && userActivityFragment.isVisible())
                            break;

                        loadUserActivityFragment();
                        break;
                    case 2:
                        UserChatFragment userChatFragment = (UserChatFragment) getFragmentManager().findFragmentByTag("FR_USERCHAT");
                        if (userChatFragment != null && userChatFragment.isVisible())
                            break;

                        loadUserChatFragment();
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
            {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finishAndRemoveTask();
                }
                else
                {
                    Intent serviceIntent = new Intent(this, LocationService.class);
                    stopService(serviceIntent);
                    startService(serviceIntent);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver,
                new IntentFilter(LocationService.KEY_LOCATION_UPDATE));
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            DbEntry entry = new DbEntry(
                                Profile.getCurrentProfile().getId(),
                                extras.getDouble("LOC_LNG", 0.0),
                                extras.getDouble("LOC_LAT", 0.0),
                                System.currentTimeMillis());

            FirebaseDatabase firebaseDbRoot = Util.getDb();
            DatabaseReference locations = firebaseDbRoot.getReference("locations");
            locations.child(Profile.getCurrentProfile().getId()).setValue(entry);
            Log.d(TAG, "Location sent to database");
        }
    };

    public void getFacebookFriends() {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                String.format("/%s/friends", Profile.getCurrentProfile().getId()),
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        Log.d("GraphResponse", response.toString());

                        try {
                            facebookFriends = response.getJSONObject().getJSONArray("data");
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }
}
