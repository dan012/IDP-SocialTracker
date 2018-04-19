package cs.idp.dvornicu.socialtracker.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import cs.idp.dvornicu.socialtracker.R;
import cs.idp.dvornicu.socialtracker.UserActivity;
import cs.idp.dvornicu.socialtracker.services.LocationService;
import cs.idp.dvornicu.socialtracker.utils.DbEntry;
import cs.idp.dvornicu.socialtracker.utils.StorageManager;
import cs.idp.dvornicu.socialtracker.utils.Util;

public class UserActivityFragment extends Fragment {

    private List<String[]> activityFeed = new LinkedList<>();
    private ArrayAdapter<String[]> listAdapter;
    private ChildEventListener childEventListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Location lastLocation = Util.getStorageManager().getLocation();
        if(lastLocation != null) {
            String[] locationUpdate = new String[3];
            locationUpdate[0] = "You have updated your location";
            locationUpdate[1] = Util.timestampToDate(lastLocation.getTime());
            locationUpdate[2] = "1";

            addFeedElement(locationUpdate);
        }

        listAdapter = new ArrayAdapter<String[]>(getContext(), R.layout.list_feed_element, R.id.firstLine, activityFeed) {

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                String[] entry = activityFeed.get(position);
                TextView text = view.findViewById(R.id.firstLine);
                text.setText(entry[0]);
                text = view.findViewById(R.id.secondLine);
                text.setText(entry[1]);

                int elementType = Integer.parseInt(entry[2]);
                ImageView icon = view.findViewById(R.id.element_icon);
                switch (elementType) {
                    case 1:
                        icon.setImageResource(R.drawable.ic_explore_black_48dp);
                        break;
                    default:
                        icon.setImageResource(R.drawable.com_facebook_button_icon_blue);
                        break;
                }

                return view;
            }
        };

        ListView listFeed = view.findViewById(R.id.list_activity);
        listFeed.setAdapter(listAdapter);

        if(childEventListener != null)
            Util.getDb().getReference("locations").removeEventListener(childEventListener);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DbEntry entry = dataSnapshot.getValue(DbEntry.class);

                if(!Profile.getCurrentProfile().getId().equals(entry.userId)) {
                    Location friendLocation = new Location("");
                    friendLocation.setTime(entry.time);
                    friendLocation.setLatitude(entry.latitude);
                    friendLocation.setLongitude(entry.longitude);
                    if (!StorageManager.friendsLocations.containsKey(entry.userId))
                        StorageManager.friendsLocations.put(entry.userId, friendLocation);
                }

                updateActivityFeedForFriend(entry.userId, entry.time);

                Log.d("ChAdd", entry.toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                DbEntry entry = dataSnapshot.getValue(DbEntry.class);

                if(!Profile.getCurrentProfile().getId().equals(entry.userId)) {
                    Location friendLocation = new Location("");
                    friendLocation.setTime(entry.time);
                    friendLocation.setLatitude(entry.latitude);
                    friendLocation.setLongitude(entry.longitude);
                    if (!StorageManager.friendsLocations.containsKey(entry.userId))
                        StorageManager.friendsLocations.put(entry.userId, friendLocation);
                }

                updateActivityFeedForFriend(entry.userId, entry.time);

                Log.d("ChUpdate", entry.toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        Util.getDb().getReference("locations").removeEventListener(childEventListener);
        Util.getDb().getReference("locations").addChildEventListener(childEventListener);
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
    public void onDestroy() {
        super.onDestroy();
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] locationUpdate = new String[3];
            locationUpdate[0] = "You have updated your location";
            locationUpdate[1] = Util.timestampToDate(System.currentTimeMillis());
            locationUpdate[2] = "1";

            addFeedElement(locationUpdate);
            listAdapter.notifyDataSetChanged();
        }
    };

    private void addFeedElement(String[] element) {
        while(activityFeed.size() >= 100) {
            activityFeed.remove(99);
        }

        activityFeed.add(0, element);
    }

    private void updateActivityFeedForFriend(String friendId, long timestamp) {
        for(int i = 0; i < UserActivity.facebookFriends.length(); ++i) {
            JSONObject friend;
            String[] locationUpdate;
            try {
                friend = UserActivity.facebookFriends.getJSONObject(i);

                if(!friend.getString("id").equals(friendId))
                    continue;

                locationUpdate = new String[3];
                locationUpdate[0] = friend.getString("name") + " has updated their location";
                locationUpdate[1] = Util.timestampToDate(timestamp);
                locationUpdate[2] = "1";
                addFeedElement(locationUpdate);
                listAdapter.notifyDataSetChanged();

                if (Profile.getCurrentProfile().getId().equals(friendId))
                    break;

                Location friendLocation = StorageManager.friendsLocations.get(friendId);
                if(friendLocation != null) {
                    friendLocation.setProvider(friend.getString("name"));
                    StorageManager.friendsLocations.remove(friendId);
                    StorageManager.friendsLocations.put(friendId, friendLocation);
                }
                break;
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

}
