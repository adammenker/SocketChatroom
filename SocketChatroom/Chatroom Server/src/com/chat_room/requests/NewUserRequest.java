package com.chat_room.requests;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class NewUserRequest {
    // gets input from the client
    // verifies if there is already a user with input username
    // writes to users file if passes validations
    public Boolean createNewUser(String username, String password, ArrayList<String> fileData, String filename) {

        if (!checkForUsernameInFile(username, fileData)) {
            // writes username and password of new user to file
            try {
                FileWriter fileWriter = new FileWriter(filename, true);
                String userRecord = "\n(" + username + ", " + password + ")";
                fileWriter.write(userRecord);
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Denied. Error occurred.");
                return false;
            }

            System.out.println("New user account created.");
            return true;
        } else {
            return false;
        }
    }

    // verifies that the input username is not already taken
    public Boolean checkForUsernameInFile(String username, ArrayList<String> fileData) {
        Boolean isUsernameInFile = false;
        for (String line : fileData) {
            if (line.equals("")) {
                continue;
            }
            String currentUsername = line.split(",")[0].substring(1);
            if (currentUsername.equals(username)) {
                isUsernameInFile = true;
            }
        }
        return isUsernameInFile;
    }
}
