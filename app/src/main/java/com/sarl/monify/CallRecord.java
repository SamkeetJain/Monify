package com.sarl.monify;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by knightshade on 11/2/17.
 */

public class CallRecord {
    private String number, dateAndTime, duration;

    public CallRecord(String number, String dateAndTime, String duration) {
        this.number = number;
        this.dateAndTime = dateAndTime;
        this.duration = duration;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "CallRecord{" +
                "number='" + number + '\'' +
                ", dateAndTime='" + dateAndTime + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public JSONObject toJson() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("number", number);
        jsonObject.put("dateAndTime", dateAndTime);
        jsonObject.put("duration", duration);
        return jsonObject;
    }
}
