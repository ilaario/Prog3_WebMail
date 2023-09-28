package com.prog3.email.prog3_webmail.Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerLayout.xml"));
        Scene clientScene = new Scene(loader.load(), 400, 200);
        stage.setTitle("Server log");
        stage.setScene(clientScene);
        stage.show();
        clientScene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (WindowEvent event) -> System.exit(1));
    }

    public static void main(String[] args) {
        launch();
    }
}
