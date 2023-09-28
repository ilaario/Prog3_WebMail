package com.prog3.email.prog3_webmail.Client;

import com.prog3.email.prog3_webmail.Client.Mail;
import com.prog3.email.prog3_webmail.Client.User;
import com.prog3.email.prog3_webmail.Utilities.Email;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class NewMessageController {
    @FXML
    private Label to;

    @FXML
    private Label subject;

    @FXML
    private TextArea txtArea;

    @FXML
    private TextField subField;

    @FXML
    private TextField toField;

    @FXML
    private Label from;

    @FXML
    private Label mailFrom;

    @FXML
    private Button btnSend;

    private Stage dialog;
    private Mail mail;
    private boolean okClicked = false;
    private ClientController cc;
    public User user;

    public MailBoxController mailContainerController;

    public void setController(ClientController cc, User user, MailBoxController mailContainerController) {
        this.cc = cc;
        this.user = user;
        this.mailContainerController = mailContainerController;
    }

    @FXML
    private void initialize() {

    }

    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }

    public void setMail(Mail mail) {
        this.mail = mail;

        if (mail == null) {
            this.mail = new Mail("", "", "", null, LocalDateTime.now(), "");
        }
        toField.setText(this.mail.getReceiversString());
        subField.setText(this.mail.getSubject());
        txtArea.setText(this.mail.getMessage());
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    /*
     * @brief: when the user click "send", check if the fields are filled and valid,
     * if not, display an error message
     *
     * @throws InterruptedException
     */
    @FXML
    private  void handleOk() throws InterruptedException {
        LoginController clientMain = new LoginController();

        String sender = mail.getSender();
        System.out.println("[NMC] sender is: " + mail.getSender());
        mail.setSender(toField.getText());
        System.out.println("[NMC] receiver field is: " + toField.getText());
        mail.setSubject(subField.getText());
        mail.setMessage(txtArea.getText());
        ArrayList<String> receivers = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        receivers.addAll(Arrays.asList(toField.getText().split("; ")));
        Mail m = new Mail("", sender, subField.getText(), toField.getText(), now,
                txtArea.getText());
        System.out.println("[NewMessageController] handleOk() m: " + m);
        Email e = new Email(sender, receivers, toField.getText(), txtArea.getText(), now);

        System.out.println("[NewMessageController] handleOk() e: " + e);

        if (isInputOk(m)) {
            // send mail
            mailSendedFeedback();
            boolean response = cc.sendMail(e, clientMain);
            if (response) {
                this.mailContainerController.updateOutboxEmails(e);
                okClicked = true;

            }
        }


    }

    public void mailSendedFeedback() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mail is going to be sent");
        alert.setHeaderText("Mail will be sent to " + toField.getText());
        alert.setContentText("Mail will be  sent to " + toField.getText());
        alert.showAndWait();
    }

    public boolean isInputOk(Mail mail) {
        String error = "";
        if (toField.getText() == null || toField.getText().length() == 0)
            error += "Missing receiver\n";

        else if (!toField.getText().contains("@javamail.it"))
            error += "Invalid receiver email format\n";

        if (mail.getReceivers().size() == 0)
            error += "Wrong email format\n";

        if (subField.getText() == null || subField.getText().length() == 0)
            error += "Missing subject\n";

        if (txtArea.getText() == null || txtArea.getText().length() == 0)
            error += "Empty message body\n";

        if (error.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialog);
            alert.setTitle("Invalid fields");
            alert.setHeaderText("Errors detected in the following fields");
            alert.setContentText(error);
            alert.showAndWait();
            return false;
        }
    }


}
