package com.example.conor.messenger3;

import com.github.nkzawa.socketio.client.Socket;

public class socketHandler {

    private static Socket socket;

    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(Socket socket){
        socketHandler.socket = socket;
    }
}
