package com.chat_room.server;

import com.chat_room.client.ConnectionHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


/*
 *
 * PLEASE REFER TO THE README BEFORE REVIEWING THE PROJECT
 *
 */


public class Server {
    // create constants
    private static final Integer PORT_NUMBER = 10824;
    private static final Integer MAXCLIENTS = 3;

    private ServerSocket serverSocket;
    private Integer baseLineNumberOfThreads = Thread.activeCount();

    // constructs a new server that contains a server socket
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    // starting point of backend
    // creates server and starts it
    public static void main(String[] args) throws IOException {
        System.out.println("My chat room client. Version Two\n");

        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        Server server = new Server(serverSocket);
        server.start();
    }

    // starts running the server instantiated in main
    public void start() {
        try {
            // continuously iterates until server socker is closed
            while(!serverSocket.isClosed()) {
                // checks if currently at maximum number of client sessions
                // only accepts another connection if there are less connected
                //      clients than the max amount
                if (Thread.activeCount() - baseLineNumberOfThreads < MAXCLIENTS) {
                    Socket socket = serverSocket.accept();
                    ConnectionHandler connectionHandler = new ConnectionHandler(socket);

                    Thread thread = new Thread(connectionHandler);
                    // starts the connectionHandler
                    thread.start();
                }
            }
        } catch (Exception e) {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
