package com.prog3.email.prog3_webmail.Client;

import com.prog3.email.prog3_webmail.Utilities.CS_Comm;
import com.prog3.email.prog3_webmail.Utilities.Email;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClientController implements Serializable {
    private String username;
    boolean serverStatus = false;
    private static Socket socket;

    private User userModel;
    Stage topStage;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;
    public static LoginController loginController = new LoginController();

    public ClientController(User userModel) {
        this.username = userModel.getUsername();
        this.userModel = userModel;
    }


    public boolean checkConnection() {
        if (serverStatus){
            if(socket != null && socket.isConnected() && !(socket.isClosed())){
                return true;
            } else {
                return connectToSocket();
            }

        }
        return connectToSocket();
    }

    private boolean connectToSocket() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            socket = new Socket(hostName, 8189);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            this.serverStatus = true;
        } catch (IOException e) {
            if(serverStatus)
                showServerDownNotification();
            this.serverStatus = false;
        }
        return serverStatus;
    }

    private static void closeSocketConnection() throws Exception {
        if (socket != null) {
            out.close();
            in.close();
            socket.close();
        }
    }

    public void showServerDownNotification() {
        Alert popup = new Alert(Alert.AlertType.ERROR);
        popup.initOwner(topStage);
        popup.setTitle("Server down");
        popup.setHeaderText("Server is down");
        popup.setContentText("Server is down, please try again later");
        popup.show();
    }

    public void showServerUpNotification() {
        Platform.runLater(() -> {
            Alert popup = new Alert(Alert.AlertType.INFORMATION);
            popup.initOwner(topStage);
            popup.setTitle("Server up");
            popup.setHeaderText("Server is up");
            popup.setContentText("Server is up, you can use the application");
            popup.show();
        });
    }

    public void mailNotExist() {
        Alert popup = new Alert(Alert.AlertType.ERROR);
        popup.initOwner(topStage);
        popup.setTitle("Mail not exist");
        popup.setHeaderText("Mail not exist");
        popup.setContentText("Mail not exist, try it again");
        popup.show();
    }

    private static CS_Comm sendCMToServer(CS_Comm c) {
        try {
            if (out == null || in == null) {
                return null;
            }
            out.writeObject(c);

            CS_Comm response = (CS_Comm) in.readObject();


            return response;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int requestInbox(){
        try {
            if (!connectToSocket()) {
                showServerDownNotification();
                return -1;
            }
            CS_Comm request = new CS_Comm("inbox", new Pair<>(username,(ArrayList)userModel.getInbox()));
            CS_Comm response = sendCMToServer(request);
            if (response == null) {
                return -1;
            }
            Object body = response.getData();
            if (!(body instanceof ArrayList)) {
                return -1;
            }
            ArrayList<Email> res = (ArrayList<Email>) body;
            if(!res.isEmpty()) {
                ObservableList<Email> resList = FXCollections.observableList(res);
                //notificationManager();
                this.userModel.addToInbox(resList);
            }
            closeSocketConnection();
            return res.size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int requestOutbox() {
        try {
            if (!connectToSocket()) {
                showServerDownNotification();
                return -1;
            }
            CS_Comm request = new CS_Comm("outbox", new Pair<>(username,(ArrayList)userModel.getOutbox()));
            CS_Comm response = sendCMToServer(request);
            if (response == null) {
                return -1;
            }
            Object body = response.getData();
            if (!(body instanceof ArrayList)) {
                return -1;
            }
            ArrayList<Email> res = (ArrayList<Email>) body;
            if(!res.isEmpty()) {
                ObservableList<Email> resList = FXCollections.observableList(res);
                this.userModel.addToOutbox(resList);
            }
            closeSocketConnection();
            return res.size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void notificationManager(){
        String[] command = {"notify-send",this.username + " received a new mail", "check your inbox"};
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void login(){
        try {
            if(!connectToSocket()){
                showServerDownNotification();
                return;
            }

            CS_Comm cm = new CS_Comm("login", username);
            CS_Comm cmFromServer = sendCMToServer(cm);
            closeSocketConnection();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean sendMail(Email mail, LoginController clientMain) {
        try {
            if (!connectToSocket()) {
                showServerDownNotification();
                return false;
            }
            CS_Comm sendMail = new CS_Comm("send", mail);
            CS_Comm response = (CS_Comm) sendCMToServer(sendMail);
            assert response != null;
            if (response.getCommand().equals("send_not_ok")) {
                mailNotExist();
                closeSocketConnection();
                return false;
            }
            closeSocketConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteMail(Mail selectedMail) {
        try {
            if (!connectToSocket()) {
                showServerDownNotification();
                return false;
            }

            ArrayList<String> receivers = (ArrayList<String>) selectedMail.getReceivers().stream().map(receiver -> receiver)
                    .collect(Collectors.toList());

            Email e = new Email(selectedMail.getSender(), receivers, selectedMail.getSubject(), selectedMail.getMessage(), selectedMail.getDate());

            Pair<String, Email> pair = new Pair(this.username, e);
            CS_Comm deleteMail = new CS_Comm("delete", pair);
            CS_Comm response = sendCMToServer(deleteMail);
            if (response.getCommand().equals("delete_not_ok")) {
                mailNotExist();
                closeSocketConnection();
                return false;
            }
            closeSocketConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
