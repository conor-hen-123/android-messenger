package com.example.conor.messenger3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.emitter.Emitter;

import java.net.URISyntaxException;
public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button btn;
    public socketHandler sock;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    //username and password taken in
    //password hashed and compared with stored password
    //continue to sign in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find id's
        btn = (Button)findViewById(R.id.signin);
        username = (EditText) findViewById(R.id.username);
        password =(EditText) findViewById(R.id.password);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if((!username.getText().toString().isEmpty()) && (!password.getText().toString().isEmpty())){

                    try {
                        sock.setSocket(IO.socket("http://192.168.1.150:3000/"));
                        //create connection
                        sock.getSocket().connect();
                        sock.getSocket().emit("join", username.getText().toString());

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    Intent i  = new Intent(MainActivity.this, FriendListActivity.class);

                    i.putExtra(USERNAME,username.getText().toString());
                    i.putExtra(PASSWORD, password.getText().toString());

                    startActivity(i);
                }
            }
        });

    }
}