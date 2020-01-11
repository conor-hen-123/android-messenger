package com.example.conor.messenger3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class FriendListActivity extends AppCompatActivity {

    public List<Friend> FriendList;
    public RecyclerView myRecylerView;
    public String user;
    public EditText addUser;
    public Button adding;
    public FriendAdapter friendadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        //username
        user = (String) getIntent().getExtras().getString(MainActivity.USERNAME);

        //user to add
        addUser = (EditText) findViewById(R.id.addUser);

        //button to add user
        adding = (Button)findViewById(R.id.adding);

        //RecyclerView of friends
        FriendList = new ArrayList<>();
        myRecylerView = (RecyclerView) findViewById(R.id.Friendlist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());

        adding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!addUser.getText().toString().isEmpty()) {

                    Friend friend = new Friend(addUser.getText().toString());

                    FriendList.add(friend);

                    //reset the username to add
                    addUser.setText(" ");

                    // add the new updated list to the adapter
                    friendadapter = new FriendAdapter(FriendList, v.getContext(), user);

                    // notify the adapter to update the recycler view
                    friendadapter.notifyDataSetChanged();

                    //set the adapter for the recycler view
                    myRecylerView.setAdapter(friendadapter);

                }
            }
        });

    }

}
