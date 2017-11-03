package com.sarl.monify.dataClasses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by knightshade on 11/2/17.
 */

public class Contact {
    private String name, number;

    public Contact(String name, String number){
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                '}';
    }

    public JSONObject toJson() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("number", number);
        return jsonObject;
    }
}
