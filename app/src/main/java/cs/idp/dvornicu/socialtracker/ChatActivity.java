package cs.idp.dvornicu.socialtracker;

import android.app.ActionBar;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import cs.idp.dvornicu.socialtracker.utils.DbChatEntry;
import cs.idp.dvornicu.socialtracker.utils.Util;

public class ChatActivity extends AppCompatActivity {

    String user_name, user_id;
    List<String[]> chatMessages = new ArrayList<>();
    ArrayAdapter<String[]> listAdapter;
    ListView listFeed;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent i = getIntent();
        user_name = i.getStringExtra("user_name");
        user_id = i.getStringExtra("user_id");

        ActionBar actionBar = getActionBar();
        if (actionBar == null && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(user_name);
        } else if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(user_name);
        }

        if(user_id == null) {
            Toast.makeText(this, "Error loading chat page", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*
        String[] chatEntry = new String[3];
        chatEntry[0] = user_name;
        chatEntry[1] = "Mesaj demo";
        chatEntry[2] = "2";

        chatMessages.add(chatEntry);
        chatMessages.add(chatEntry);
        chatMessages.add(chatEntry);
        chatMessages.add(chatEntry);
        chatMessages.add(chatEntry);
        chatMessages.add(chatEntry);
        chatMessages.add(chatEntry);
        chatMessages.add(chatEntry);
        */

        listAdapter = new ArrayAdapter<String[]>(this, R.layout.list_feed_element, R.id.firstLine, chatMessages) {

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                String[] entry = chatMessages.get(position);
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

        listFeed = findViewById(R.id.list_chat_messages);
        listFeed.setAdapter(listAdapter);

        Button sendBtn = findViewById(R.id.chat_send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText msgBox = findViewById(R.id.chat_msgbox);
                String msgText = msgBox.getText().toString().trim();

                if (msgText.isEmpty())
                    return;

                String[] myChatEntry = new String[3];
                myChatEntry[0] = Profile.getCurrentProfile().getName();
                myChatEntry[1] = msgText;
                myChatEntry[2] = "2";
                chatMessages.add(myChatEntry);
                listAdapter.notifyDataSetChanged();

                // Add message to local storage
                Util.getStorageManager().addMessageToHistory(user_id, myChatEntry);

                msgBox.setText(null);
                listFeed.setSelection(listAdapter.getCount() - 1);

                DbChatEntry entry = new DbChatEntry(
                        msgText,
                        System.currentTimeMillis());

                FirebaseDatabase firebaseDbRoot = Util.getDb();
                DatabaseReference myChat = firebaseDbRoot.getReference("chat").child(Profile.getCurrentProfile().getId());
                myChat.child(user_id).setValue(entry);
            }
        });

        List<String[]> chatHistory = Util.getStorageManager().getChatHistory(user_id);
        if(chatHistory != null) {
            chatMessages.addAll(chatHistory);
            listAdapter.notifyDataSetChanged();
        }

        listFeed.setSelection(listAdapter.getCount() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DbChatEntry entry = dataSnapshot.getValue(DbChatEntry.class);

                // Check message receiver
                if(!Profile.getCurrentProfile().getId().equals(dataSnapshot.getKey())) {
                    return;
                }

                // Add message from friend to chat
                String[] myChatEntry = new String[3];
                myChatEntry[0] = user_name;
                myChatEntry[1] = entry.message;
                myChatEntry[2] = "2";
                chatMessages.add(myChatEntry);
                listAdapter.notifyDataSetChanged();

                // Add message to local storage
                Util.getStorageManager().addMessageToHistory(user_id, myChatEntry);

                Log.d("ChAdd", entry.toString() + " || " + dataSnapshot.getKey());

                dataSnapshot.getRef().setValue(null);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                DbChatEntry entry = dataSnapshot.getValue(DbChatEntry.class);

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
        Util.getDb().getReference("chat").child(user_id).addChildEventListener(childEventListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
