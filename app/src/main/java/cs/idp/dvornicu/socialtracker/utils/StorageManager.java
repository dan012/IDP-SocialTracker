package cs.idp.dvornicu.socialtracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StorageManager {

    private SharedPreferences mSharedPref;

    public static HashMap<String, Location> friendsLocations = new HashMap<>();
    private static HashMap<String, List<String[]>> chatHistory = new HashMap<>();

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

    public void addMessageToHistory(String repository, String[] entry) {
        List<String[]> currentHistory = chatHistory.get(repository);
        if (currentHistory == null)
            currentHistory = new ArrayList<>();

        if (currentHistory.size() > 150)
            currentHistory.remove(0);

        currentHistory.add(entry);
        chatHistory.put(repository, currentHistory);
    }

    public List<String[]> getChatHistory(String repository) {
        return chatHistory.get(repository);
    }
}
