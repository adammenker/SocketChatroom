package com.chat_room.requests;

public class NewUserRequest {

    // validates user input for running newuser command
    // checks number of arguments, argument length, and invalid characters
    public Boolean isValidCreateNewUserRequest(String[] inputArray) {
        if (inputArray.length != 3) {
            return false;
        }

        String username = inputArray[1];
        if (username.length() < 3 || username.length() > 32 || username.contains(",")) {
            return false;
        }

        String password = inputArray[2];
        if (password.length() < 4 || password.length() > 8 || password.contains(" ")) {
            return false;
        }
        return true;
    }
}
