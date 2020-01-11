package com.example.conor.messenger3;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


    public class ChatBoxAdapter  extends RecyclerView.Adapter<ChatBoxAdapter.MyViewHolder> {
        private List<Message> MessageList;

        public  class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView username;
            public TextView message;

            public MyViewHolder(View view) {
                super(view);
                username = (TextView) view.findViewById(R.id.username);
                message = (TextView) view.findViewById(R.id.message);
            }
        }

        public ChatBoxAdapter(List<Message>MessagesList) {
            this.MessageList = MessagesList;
        }

        @Override
        public int getItemCount() {
            return MessageList.size();
        }

        @Override
        public ChatBoxAdapter.MyViewHolder onCreateViewHolder(ViewGroup    parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ChatBoxAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ChatBoxAdapter.MyViewHolder holder, final int position) {
            Message m = MessageList.get(position);
            holder.username.setText(m.getUsername());
            holder.message.setText(m.getMessage() );
        }
        }