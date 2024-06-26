package com.prog3.email.prog3_webmail.Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ClientMain extends Application{

    @Override
    public void start(Stage stage){
        try {
            stage.setTitle("Client mail");
            FXMLLoader loaderLogin = new FXMLLoader(getClass().getResource("FrontPage.fxml"));
            Scene sceneLogin = new Scene(loaderLogin.load(), 650, 460);

            stage.setTitle("Login");
            stage.setScene(sceneLogin);

            // stage.setResizable(false);

            stage.setOnCloseRequest(windowEvent -> Platform.exit());
            sceneLogin.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (WindowEvent e) -> System.exit(1));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        launch();
    }
}
