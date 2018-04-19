package cs.idp.dvornicu.socialtracker.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService";
    public static final String KEY_LOCATION_UPDATE = "key_location_update";

    private boolean currentlyProcessingLocation = false;
    private GoogleApiClient googleApiClient;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }

        return START_NOT_STICKY;
    }

    private void setUpLocationClientIfNeeded() {
        if(googleApiClient != null) return;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private boolean checkGoogleAPIAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        return (resultCode == ConnectionResult.SUCCESS);
    }

    private void startTracking() {
        if (!checkGoogleAPIAvailable()) return;

        setUpLocationClientIfNeeded();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5 * 1000);
        mLocationRequest.setFastestInterval(2 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(googleApiClient != null && googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;
        Log.d(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

        if (location.getAccuracy() >= 500.0f) return;

        Intent locationUpdateIntent = new Intent(KEY_LOCATION_UPDATE);
        locationUpdateIntent.putExtra("LOC_LNG", location.getLongitude());
        locationUpdateIntent.putExtra("LOC_LAT", location.getLatitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationUpdateIntent);

        setLocationAlarm();
        this.stopSelf();
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, LocationService.this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        final LocationRequest locationRequest = createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            break;
                        }
                        if(googleApiClient == null) break;
                        //Log.d(TAG, "Location requested.");
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, LocationService.this);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "Change unavailable");
                        break;
                    default:
                        Log.d(TAG, "Default case" + status.getStatusMessage() + " " + status.getStatusCode());
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApiClient connection has been suspended.");
    }

    private void setLocationAlarm() {
        PendingIntent wakeupIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, new Intent(getApplicationContext(), LocationAlarmReceiver.class), 0);
        if(Build.VERSION.SDK_INT >= 23)
        {
            ((AlarmManager)getSystemService(Context.ALARM_SERVICE)).setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 20 * 1000, wakeupIntent);
        }
        else {
            ((AlarmManager)getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 20 * 1000,
                    wakeupIntent);
        }
    }
}