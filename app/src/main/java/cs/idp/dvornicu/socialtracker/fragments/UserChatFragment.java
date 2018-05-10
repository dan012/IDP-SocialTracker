package cs.idp.dvornicu.socialtracker.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cs.idp.dvornicu.socialtracker.ChatActivity;
import cs.idp.dvornicu.socialtracker.R;
import cs.idp.dvornicu.socialtracker.UserActivity;

public class UserChatFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<String> fbFriends = new ArrayList<>();
        for(int i = 0; i < UserActivity.facebookFriends.length(); ++i) {
            JSONObject friend;
            try {
                friend = UserActivity.facebookFriends.getJSONObject(i);

                fbFriends.add(friend.getString("name"));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, fbFriends.toArray(new String[0]));
        ListView listFeed = view.findViewById(R.id.list_chat_friends);
        listFeed.setAdapter(listAdapter);

        listFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject friend;

                try {
                    friend = UserActivity.facebookFriends.getJSONObject(i);
                    Toast.makeText(getContext(), "My text is " + friend.getString("name"), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("user_name", friend.getString("name"));
                    intent.putExtra("user_id", friend.getString("id"));
                    getActivity().startActivity(intent);

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                String selectedItem = (String) adapterView.getItemAtPosition(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
