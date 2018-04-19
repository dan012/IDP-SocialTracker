package cs.idp.dvornicu.socialtracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.Nullable;

import org.json.JSONArray;

import java.util.HashMap;

public class StorageManager {

    private SharedPreferences mSharedPref;

    public static HashMap<String, Location> friendsLocations = new HashMap<>();

    public StorageManager(Context c) {
        mSharedPref = c.getSharedPreferences("StorageManager", Context.MODE_PRIVATE);
    }

    public synchronized void setLocation(Location location) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putFloat("last_location_lat", (float) location.getLatitude());
        editor.putFloat("last_location_lng", (float) location.getLongitude());
        editor.putLong("last_location_time", location.getTime());
        editor.apply();
    }

    @Nullable
    public Location getLocation() {
        float lat = mSharedPref.getFloat("last_location_lat", 0);
        float lon = mSharedPref.getFloat("last_location_lng", 0);
        long time = mSharedPref.getLong("last_location_time", 0);

        if (time == 0) return null;

        Location location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setTime(time);
        return location;
    }
}
