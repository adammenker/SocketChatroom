package com.chat_room.client;

import com.chat_room.requests.LoginRequest;
import com.chat_room.requests.NewUserRequest;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ConnectionHandler implements Runnable {
    // creates variables for constants and initializes reader, writer, and socket
    private static final String FILENAME = "resources/users.txt";
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    // Array list that will hold the individual client connections
    public static ArrayList<ConnectionHandler> connectionHandlers = new ArrayList<>();

    private Boolean hasLoggedOut;
    private String clientUsername;

    public ConnectionHandler (Socket socket) {
        try {
            // upon instantiation, create any dependecies the handler will need
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.hasLoggedOut = false;

        } catch (Exception e) {
            endSession(socket, bufferedReader, bufferedWriter);
        }
    }

    // Class implements Runnable so it must override the run method
    @Override
    public void run() {
        String messageFromClient;
        // iterates continuously until the connection has been ended
        while (socket.isConnected()) {
            try {
                // connection ends through user logging out
                if (hasLoggedOut) {
                    return;
                }

                // connection ends by terminating the client
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null) {
                    throw new Exception();
                }

                // delegates which command will run based off of input
                parseMessage(messageFromClient);
            } catch (Exception e) {
                endSession(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    // takes the message from the client calls corresponding methods for
    // the command the user provided
    public void parseMessage(String message) {
        Boolean requestSuccessful;
        // separate input into readable components
        String[] splitMessageArray = message.trim().split("\\s+");
        // takes in command
        switch (splitMessageArray[0]) {
            case "login":
                // do not allow multiple clients to log into the same user
                Boolean alreadyLoggedIn = false;
                for (ConnectionHandler connectionHandler : connectionHandlers) {
                    if (splitMessageArray[1].equals(connectionHandler.clientUsername)) {
                        sendResponseMessage("RESPONSE: Denied. User is already logged in on another client.");
                        alreadyLoggedIn = true;
                    }
                }
                if (alreadyLoggedIn) {
                    break;
                }
                LoginRequest loginRequest = new LoginRequest();
                requestSuccessful = loginRequest.login(splitMessageArray[1], splitMessageArray[2], getFileLines(FILENAME));
                // if successfully logged in, add client to list of handlers and send message to other clients
                if (requestSuccessful) {
                    clientUsername = splitMessageArray[1];
                    connectionHandlers.add(this);
                    broadcastMessage(clientUsername + " joins.");
                    sendResponseMessage("RESPONSE: login confirmed");
                } else {
                    sendResponseMessage("RESPONSE: Denied. Username or password incorrect");
                }
                break;
            case "newuser":
                // write a new user to the users.txt file
                NewUserRequest newUserRequest = new NewUserRequest();
                requestSuccessful = newUserRequest.createNewUser(splitMessageArray[1], splitMessageArray[2], getFileLines(FILENAME), FILENAME);
                if (requestSuccessful) {
                    sendResponseMessage("RESPONSE: New user account created. Please login");
                } else {
                    sendResponseMessage("RESPONSE: Denied. User account already exists.");
                }
                break;
            case "send":
                // concatenate a string that represents the desired message
                String messageToSend = this.clientUsername + ": " + message.split(" ", 3)[2];
                // send message to all clients if keyword "all" is used in command
                if (splitMessageArray[1].equals("all")) {
                    broadcastMessage(messageToSend);
                    System.out.println(messageToSend);
                    break;
                } else {
                    // otherwise send message to individual user and give response back to sender
                    if (splitMessageArray[1].equals(this.clientUsername)) {
                        sendResponseMessage("RESPONSE: Denied. You can not unicast a message to yourself.");
                        break;
                    }
                    Boolean usernameInFile = new NewUserRequest().checkForUsernameInFile(splitMessageArray[1], getFileLines(FILENAME));
                    if (usernameInFile) {
                        System.out.println(clientUsername + " (to " + splitMessageArray[1] + "): " + message.split(" ", 3)[2]);
                        unicastMessage(messageToSend, splitMessageArray[1]);
                    } else {
                        sendResponseMessage("RESPONSE: Denied. There is no user with that Username.");
                    }
                    break;
                }
            case "who":
                // combines all the names of logged on users into one string representing a list
                String usersConnected = "";
                for (ConnectionHandler connection : connectionHandlers) {
                    usersConnected = usersConnected + connection.clientUsername + ", ";
                }
                // cuts off extraneous trailing comma and sends message to client
                sendResponseMessage("RESPONSE: " + usersConnected.substring(0, usersConnected.length() - 2));
                break;
            case "logout":
                // ends to session by freeing resources, sending logout response to client, and setting backend
                // state to reflect that the user has logged out
                System.out.println(clientUsername + " logout");
                broadcastMessage(clientUsername + " left.");
                this.hasLoggedOut = true;
                sendResponseMessage("RESPONSE: logout");
                endSession(socket, bufferedReader, bufferedWriter);
                break;
        }
    }

    // reads file data and returns an array list of text
    public ArrayList<String> getFileLines(String filename) {
        ArrayList<String> lines = new ArrayList<>();
        // converts textfile into easily manageable data
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            // skips white space
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return lines;
    }

    // sends message to all connected clients
    public void broadcastMessage(String messageToSend) {
        // skips the sending client
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            try {
                if (!connectionHandler.clientUsername.equals(clientUsername)) {
                    connectionHandler.bufferedWriter.write(messageToSend);
                    connectionHandler.bufferedWriter.newLine();
                    connectionHandler.bufferedWriter.flush();
                }
            } catch (Exception e) {
                endSession(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // sends message directly to client specified by username
    public void unicastMessage(String messageToSend, String username) {
        // searches through clients until it finds the desired one
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            try {
                if (connectionHandler.clientUsername.equals(username)) {
                    connectionHandler.bufferedWriter.write(messageToSend);
                    connectionHandler.bufferedWriter.newLine();
                    connectionHandler.bufferedWriter.flush();
                }
            } catch (Exception e) {
                endSession(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // sends message back to sender
    // allows the sender to see the status of their request
    public void sendResponseMessage(String message) {
        try {
            this.bufferedWriter.write(message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (Exception e) {
            endSession(socket, bufferedReader, bufferedWriter);
        }
    }

    // removes the connection from the list of online clients
    public void removeConnectionHandler() {
        connectionHandlers.remove(this);
    }

    // closes any resources that need to be let go of
    public void endSession(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeConnectionHandler();
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
    }

    // getter for the connections username
    public String getClientUsername() {
        return clientUsername;
    }
}
