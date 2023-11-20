package com.prog3.email.prog3_webmail.Utilities;

import java.io.Serializable;

/*
 * @brief: this class handle the communication between client and server
 * @param: action: the action that the client want to do, like login, request inbox, send email, etc.
 * @param: body: the body of the action, like the email that the client want to send, the user that want to login, etc.
 * */

public class CS_Comm implements Serializable {
    private String command;

    private Object data;

    public CS_Comm(String action, Object body) {
        this.command = action;
        this.data = body;
    }

    public String getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }
}