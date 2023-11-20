package com.prog3.email.prog3_webmail.Utilities;

import java.io.Serializable;
import java.util.ArrayList;

public class LoginResponse implements Serializable {
    private ArrayList<ArrayList<Email>> arrayLists = new ArrayList<>();

    public LoginResponse(){}

    public LoginResponse(ArrayList<ArrayList<Email>> arrayLists) {
        this.arrayLists = arrayLists;
    }


}
