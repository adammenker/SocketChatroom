package com.chat_room.client;

import com.chat_room.requests.LoginRequest;
import com.chat_room.requests.NewUserRequest;
import com.chat_room.requests.SendRequest;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


/*
*
* PLEASE REFER TO THE README BEFORE REVIEWING THE PROJECT
*
 */


public class Client {
    // sets constants
    private static final Integer PORT_NUMBER = 10824;

    // creates variables that will be instantiated in the constructor
    Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;

    Boolean isLoggedIn;

    // makes a client that contains its own reader, writer, and socket
    // each client represents a connection on the backend
    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.isLoggedIn = false;
        } catch (Exception e) {
            e.printStackTrace();
            endSession(socket, bufferedReader, bufferedWriter);
        }
    }

    // starting point of the frontend (client side)
    public static void main(String[] args) throws IOException {
        System.out.println("My chat room client. Version Two\n");

        Socket socket = new Socket("127.0.0.1", PORT_NUMBER);
        Client client = new Client(socket);

        // these methods allow for a constant line of communication
        //      to be formed between the client and the server
        // anything the client types will be read by the server
        // and anything the server responds with can be grabbed by the client
        client.listenForMessage();
        client.sendMessage();
    }


    // checks to see if the command the user entered satisfies the requirements
    // recursively calls itself until a valid command in input
    public String getValidatedCommand(String message, Scanner scanner) {
        // splits up the message into more manageable data
        String[] splitInputArray = message.trim().split("\\s+");
        String command = splitInputArray[0];

        // for each command verify if it:
                // has the right number of arguments
                // the arguments do not break any rules
                // the user has the correct permissions to execute that command
        // the logic is the same for each command, but handled slightly different
        //      based on what the command is asking for/what it does
        switch (command) {
            // check keyword
            case "login":
                // check permission
                if (!isLoggedIn) {
                    // check if request is valid
                    LoginRequest loginRequest = new LoginRequest();
                    if (!loginRequest.isValidLoginRequest(splitInputArray)) {
                        // give error message if invalid command
                        System.out.println("> Denied. Invalid input to logging in.");
                        message = readLine(scanner);
                        // iteratively call itself to validate another command
                        return getValidatedCommand(message, scanner);
                    }
                } else {
                    System.out.println("> Denied. You must logout first in order to login.");
                    message = readLine(scanner);
                    return getValidatedCommand(message, scanner);
                }
                break;
            case "newuser":
                if (!isLoggedIn) {
                    NewUserRequest newUserRequest = new NewUserRequest();
                    if (!newUserRequest.isValidCreateNewUserRequest(splitInputArray)) {
                        System.out.println("> Denied. Invalid input to creating a new user.");
                        message = readLine(scanner);
                        return getValidatedCommand(message, scanner);
                    }
                } else {
                    System.out.println("> Denied. You must logout first in order to create a new user.");
                    message = readLine(scanner);
                    return getValidatedCommand(message, scanner);
                }
                break;
            case "send":
                if (isLoggedIn) {
                    SendRequest sendRequest = new SendRequest();
                    if (!sendRequest.isValidSendRequest(message.split(" ", 3))) {
                        System.out.println("> Denied. Please login first.");
                        message = readLine(scanner);
                        return getValidatedCommand(message, scanner);
                    }
                } else {
                    System.out.println("> Denied. Please login first.");
                    message = readLine(scanner);
                    return getValidatedCommand(message, scanner);
                }
                break;
            case "who":
                if (isLoggedIn) {
                    if (splitInputArray.length != 1) {
                        System.out.println("> Denied. Invalid input to see who is online.");
                        message = readLine(scanner);
                        return getValidatedCommand(message, scanner);
                    }
                }  else {
                    System.out.println("> Denied. Please login first.");
                    message = readLine(scanner);
                    return getValidatedCommand(message, scanner);
                }
                break;
            case "logout":
                if (isLoggedIn) {
                    if (splitInputArray.length != 1) {
                        System.out.println("> Denied. Invalid input to logout.");
                        message = readLine(scanner);
                        return getValidatedCommand(message, scanner);
                    }
                } else {
                    System.out.println("> Denied. Please login first.");
                    message = readLine(scanner);
                    return getValidatedCommand(message, scanner);
                }
                break;
            default:
                System.out.println("> Denied. Invalid command.");
                message = readLine(scanner);
                return getValidatedCommand(message, scanner);
        }

        // if control flow gets here, it means message was handled in one of the
        // cases and did not cause an error, this allowed control to break out of switch
        // and hit this return statement
        return message;
    }

    // gets the text from the console
    public String readLine(Scanner scanner) {
        System.out.print("> ");
        return scanner.nextLine();
    }

    // writes a message to the server
    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);
            // continuously runs while the client is still connected
            while (socket.isConnected()) {
                // validates input and then sends to server
                String message = getValidatedCommand(readLine(scanner), scanner);
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();

            }
        } catch (Exception e) {
            endSession(socket, bufferedReader, bufferedWriter);
        }
    }

    // reads responses from the server
    public void listenForMessage() {
        new Thread(() -> {
            String msg;
            // continuously looks for a message to be sent back from the server
            // and then handles the message
            // keeps going until client's session ends
            while (socket.isConnected()) {
                try {
                    msg = bufferedReader.readLine();
                    if (msg == null) {
                        break;
                    }
                    // special case in which the server is giving a message that is for the
                    // client to process instead of simply logging it to the console

                    // for when a message is sent back to the sender
                    if (msg.startsWith("RESPONSE: ")) {
                        processServerResponse(msg.substring("RESPONSE: ".length()));
                    } else {
                        System.out.println(msg);
                    }
                    System.out.print("> ");
                } catch (Exception e) {
                    endSession(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    // takes in a message and sets the state of the client based
    public void processServerResponse(String serverResponse) {
        switch (serverResponse) {
            case "login confirmed":
                System.out.println(serverResponse);
                isLoggedIn = true;
                break;
            case "logout":
                isLoggedIn = false;
                endSession(socket, bufferedReader, bufferedWriter);
                break;
            default:
                System.out.println(serverResponse);
                break;
        }
    }

    // closes any resources that need to be let go of
    // exits out of program
    public void endSession(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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
            System.out.println("An Error has occurred when ending the session");;
        } finally {
            System.exit(1);
        }
    }
}
