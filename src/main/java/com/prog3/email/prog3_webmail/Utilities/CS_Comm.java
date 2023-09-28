package com.prog3.email.prog3_webmail.Utilities;

import java.io.Serializable;

public class CS_Comm implements Serializable {
    private String command;
    private Object data;

    public CS_Comm(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }
}
