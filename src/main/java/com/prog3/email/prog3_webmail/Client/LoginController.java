package com.prog3.email.prog3_webmail.Client;

import com.prog3.email.prog3_webmail.Client.RootLayoutController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController {
    @FXML
    private Label titolo;

    @FXML
    private Label mail;

    @FXML
    private Label pw;

    @FXML
    private TextField mailField;

    @FXML
    private PasswordField pwField;

    @FXML
    private Button btnLogin;

    @FXML
    public BorderPane root;
    private Stage topStage;

    public String userEmail;
    public String pwString;

    public User user;
    public ClientController cc;
    public MailBoxController mailContainerController;
    private RootLayoutController controllerRoot;
    private ExecutorService serverStatus;

    private static final Pattern VALID_PASSWORD_REGEX = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[*@#$%^&+=!]).{8,}$");

    @FXML
    public void loginButton(ActionEvent event) throws IOException {
        userEmail = mailField.getText();
        user = new User(userEmail);
        pwString = pwField.getText();

        if(userEmail.equals("") || pwString.equals("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText("Please insert your email or password");
            alert.showAndWait();
            return;
        }

        if(!checkValidPassword(pwString)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText("Invalid password! \nInsert a valid password (at least 8 characters, 1 uppercase, 1 lowercase, 1 number and 1 special character)");
            alert.showAndWait();
            return;
        }
        cc = new ClientController(user);
        initRootLayout();
        cc.login();
        startServerCheckTimer();
        openMailBox();
    }

    private boolean checkValidPassword(String password) {
        Matcher m = VALID_PASSWORD_REGEX.matcher(password);
        return m.matches(); //maybe use m.matches() instead
    }

    private void checkConnection() {
        boolean connected = controllerRoot.setServerStatusLabel(cc.checkConnection());
        if (connected)
            cc.showServerUpNotification();
    }

    private void startServerCheckTimer() {
        serverStatus = Executors.newSingleThreadExecutor();
        serverStatus.execute(() -> {
            while (true) {
                try {
                    Platform.runLater(this::checkConnection);
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showSendMailDialog(Mail mail, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("SendMessages.fxml"));
            Pane page = loader.load();
            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(topStage);

            Scene scene = new Scene(page);
            dialog.setScene(scene);

            NewMessageController controller = loader.getController();
            controller.setController(cc, user, mailContainerController);
            controller.setDialog(dialog);
            controller.setMail(mail);

            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initRootLayout() {
        try {
            topStage = (Stage) root.getScene().getWindow();
            FXMLLoader loaderRoot = new FXMLLoader(ClientMain.class.getResource("RootLayout.fxml"));
            root.setTop(loaderRoot.load());

            System.out.println(userEmail);
            controllerRoot = loaderRoot.getController();
            controllerRoot.setClientMain(this, cc.checkConnection(), userEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openMailBox() {
        try {
            FXMLLoader loaderContainer = new FXMLLoader(ClientMain.class.getResource("MailBox.fxml"));

            root.setCenter(loaderContainer.load());
            topStage.setTitle(this.userEmail);
            mailContainerController = loaderContainer.getController();

            mailContainerController.setClientMain(this, this.user, this.cc);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
