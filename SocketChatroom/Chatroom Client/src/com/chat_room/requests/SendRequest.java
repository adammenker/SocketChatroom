package com.chat_room.requests;

public class SendRequest {

    // validates user input for running login command
    // checks number of arguments and argument length
    // handles cases for extraneous white space
    public Boolean isValidSendRequest(String[] inputArray) {
        if (inputArray.length < 3) {
            return false;
        } else if (inputArray[2].length() < 1 || inputArray[1].length() > 256) {
            return false;
        } else if (inputArray[1].trim().isEmpty() || inputArray[2].trim().isEmpty()){
            return false;
        } else {
            return true;
        }
    }
}
