package cs.idp.dvornicu.socialtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import cs.idp.dvornicu.socialtracker.utils.StorageManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    CallbackManager callbackManager;
    private static StorageManager sStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sStorageManager = new StorageManager(this);

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile", "user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                if(Profile.getCurrentProfile() != null) {
                    Log.d(TAG, "Profile already loaded, launching...");
                    launchUserActivity();
                    return;
                }

                ProfileTracker profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        this.stopTracking();
                        launchUserActivity();
                        Log.d(TAG, "Profile fetched, launching...");
                    }
                };
                profileTracker.startTracking();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "facebook:onError", exception);
            }
        });

        // Check if we are already logged in
        if (AccessToken.getCurrentAccessToken() != null) {
            launchUserActivity();
        }
    }

    private void launchUserActivity() {
        Intent intent = new Intent(this, UserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public static StorageManager getStorageManager()
    {
        return sStorageManager;
    }
}
