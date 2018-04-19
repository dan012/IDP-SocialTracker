package cs.idp.dvornicu.socialtracker.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;
import java.util.List;

import cs.idp.dvornicu.socialtracker.R;
import cs.idp.dvornicu.socialtracker.services.LocationService;
import cs.idp.dvornicu.socialtracker.utils.StorageManager;
import cs.idp.dvornicu.socialtracker.utils.Util;


public class UserInfoFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "UserInfoFragment";

    private Profile profile = null;
    private MapView mapView;
    private Location lastKnownLocation;
    private static boolean cameraUpdated = false;

    public UserInfoFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profile = Profile.getCurrentProfile();
        if (profile == null) {
            Log.d(TAG, "User Profile is null");
            Profile.fetchProfileForCurrentAccessToken();
            profile = Profile.getCurrentProfile();
        }

        Log.d(TAG, "Profile is " + (profile == null ? "null" : profile.getFirstName()));
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(LocationService.KEY_LOCATION_UPDATE));
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_user_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateUsername(view);

        lastKnownLocation = Util.getStorageManager().getLocation();

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        cameraUpdated = false;
    }

    private void updateUsername(@NonNull View view) {
        if (profile == null) {
            Log.d(TAG, "UpdateUsername: Null profile");
            profile = Profile.getCurrentProfile();
        }
        if (profile == null) return;

        Log.d(TAG, "UpdateUsername: " + profile.getName());

        TextView tv_username = view.findViewById(R.id.tv_username);
        tv_username.setText(profile.getName());
    }

    private void updateMap(Location location) {
        Toast.makeText(getContext(), "Location at " + location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_LONG).show();

        lastKnownLocation = location;
        lastKnownLocation.setTime(System.currentTimeMillis());

        if (mapView != null)
            mapView.getMapAsync(this);

        Util.getStorageManager().setLocation(lastKnownLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap myMap;
        myMap = googleMap;
        myMap.clear();

        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.setTrafficEnabled(false);
        myMap.setIndoorEnabled(false);
        myMap.setBuildingsEnabled(false);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(true);
        myMap.getUiSettings().setScrollGesturesEnabled(true);

        MapsInitializer.initialize(getContext());

        CameraUpdate cameraUpdate;
        if (lastKnownLocation != null) {
            googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .title(Util.timestampToDate(lastKnownLocation.getTime()))
            );

            if(!cameraUpdated) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 10.0F);
                myMap.animateCamera(cameraUpdate);
                cameraUpdated = true;
            }
        }
        else {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(44.427468, 26.103218), 10.0F);
            myMap.animateCamera(cameraUpdate);
        }

        for(Location friendLocation : StorageManager.friendsLocations.values()) {
            googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(new LatLng(friendLocation.getLatitude(), friendLocation.getLongitude()))
                    .title(friendLocation.getProvider())
                    .snippet(Util.timestampToDate(friendLocation.getTime()))
            );
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = new Location("void");
            location.setLatitude(intent.getExtras().getDouble("LOC_LAT", 0.0));
            location.setLongitude(intent.getExtras().getDouble("LOC_LNG", 0.0));
            updateMap(location);
        }
    };

}
