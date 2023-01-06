package com.chat_room.requests;

import java.util.ArrayList;

public class LoginRequest {
    String loggedInUser = new String();
    Boolean isLoggedIn = false;

    public Boolean login(String username, String password, ArrayList<String> fileData) {
        // reads username and password from client

        if (validateLogin(username, password, fileData)) {
            // sets logged in and passes success message status on successful login
            System.out.println(username + " login");
            loggedInUser = username;
            isLoggedIn = true;
            return true;
        } else {
            return false;
        }
    }

    // verifies that the input matches a record in the users file
    private static Boolean validateLogin(String username, String password, ArrayList<String> fileData) {
        Boolean isUsernameAndPasswordInFile = false;
        for (String line : fileData) {
            if (line.equals("")) {
                continue;
            }
            String currentUsername = line.split(",")[0].substring(1);
            String currentPassword = line.split(" ")[1].substring(0, line.split(" ")[1].length() - 1);
            if (currentUsername.equals(username) && currentPassword.equals(password)) {
                isUsernameAndPasswordInFile = true;
            }
        }
        return isUsernameAndPasswordInFile;
    }
}
