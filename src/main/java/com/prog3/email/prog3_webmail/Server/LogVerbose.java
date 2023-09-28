package com.prog3.email.prog3_webmail.Server;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public class LogVerbose {
    public SimpleStringProperty log;

    public LogVerbose() {
        log = new SimpleStringProperty("Server started \n");
    }

    public SimpleStringProperty getLog() {
        return log;
    }

    public void setLog(String logs) {
        Platform.runLater(() -> log.set(logs));
    }
}
