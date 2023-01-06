package com.chat_room.requests;

public class LoginRequest {

    // validates user input for running login command
    // checks number of arguments and argument length
    public Boolean isValidLoginRequest(String[] inputArray) {
        if (inputArray.length != 3) {
            return false;
        }

        String username = inputArray[1];
        if (username.length() < 3 || username.length() > 32) {
            return false;
        }

        String password = inputArray[2];
        if (password.length() < 4 || password.length() > 8) {
            return false;
        }

        return true;
    }
}
