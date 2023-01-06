package com.chat_room.requests;

import com.chat_room.client.ConnectionHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class LogoutRequest {
    // logs out user and ends session
    public Boolean logout(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, ArrayList<ConnectionHandler> connectionHandlers, ConnectionHandler connectionHandler) {

        System.out.println("CLOSING EVERYTHING");
        connectionHandler.broadcastMessage("SERVER: " + connectionHandler.getClientUsername() + " has left the chat");
        connectionHandlers.remove(connectionHandler);

        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
