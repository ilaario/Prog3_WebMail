package com.prog3.email.prog3_webmail.Server;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLayoutController {

    @FXML
    public TextFlow logFlow;

    public LogVerbose logModel;

    public ServerLayoutController() {
        logModel = new LogVerbose();
        logModel.getLog().addListener((observable, oldValue, newValue) -> {
            setLog(newValue);
        });

        Runnable serverThread = new ServerController(logModel);
        new Thread(serverThread).start();

    }

    public void initialize() {
        System.out.println("ServerLayoutController initialized");
        logModel.setLog("server started");
    }

    /*
     * @brief: here the program write on the TextFlow the log,it adds to the string the time of the log
     *
     */
    public void setLog(String log) {
        Text fullLog = new Text("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "]\t ---> "
                + "  " + log + "\n");
        fullLog.setFill(Color.web("#ffffff"));
        logFlow.getChildren().add(fullLog);
    }
}

