package com.sarl.monify.dataClasses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by knightshade on 11/2/17.
 */

public class Sms {
    private String number, message, timestamp;

    public Sms(String number, String message, String timestamp){
        this.number = number;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Sms{" +
                "number='" + number + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("number", number);
        jsonObject.put("message", message);
        jsonObject.put("timestamp", timestamp);
        return jsonObject;
    }
}
