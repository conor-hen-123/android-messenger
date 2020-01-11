package com.example.conor.messenger3;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.SocketHandler;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyViewHolder> {

    private List<Friend> friendlist;
    public Friend m;
    public String ownusername;
    public Context context;


    public Intent in;

    public  class MyViewHolder extends RecyclerView.ViewHolder {

        public Button add_user;

        public MyViewHolder(View view) {

            super(view);
            add_user = (Button) view.findViewById(R.id.add_user);
        }
    }

    public FriendAdapter(List<Friend> friendlist,  Context context ,String str) {

        this.friendlist = friendlist;
        this.context = context;
        this.ownusername = str;
    }

    @Override
    public int getItemCount() {
        return friendlist.size();
    }

    @Override
    public FriendAdapter.MyViewHolder onCreateViewHolder(ViewGroup    parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new FriendAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FriendAdapter.MyViewHolder holder, final int position) {

        m = friendlist.get(position);
        holder.add_user.setText(m.getUsername());
        holder.add_user.setOnClickListener(new View.OnClickListener(){
            @Override
           public void onClick(View view){

               in = new Intent(context, ChatBoxActivity.class);
               in.putExtra("user",ownusername);
               in.putExtra ("friend", m.getUsername());
               context.startActivity(in);

    }
     });
    }
}