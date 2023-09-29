package com.prog3.email.prog3_webmail.Client;

import com.prog3.email.prog3_webmail.Utilities.Email;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MailBoxController {

    // LABELS

    @FXML
    private Label lblSubject;

    @FXML
    private Label lblSubjectVuota;

    @FXML
    private Label lblFrom;

    @FXML
    private Label lblTo;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblFromVuota;

    @FXML
    private Label lblToVuota;

    @FXML
    private Label lblDateVuota;

    @FXML
    private Label lblNothingNewHere;

    @FXML
    private Label lblNothingNewHere2;


    // TABS

    @FXML
    private Tab receivedTab;

    @FXML
    private Tab sentTab;

    @FXML
    private TableView<Mail> inTable;

    @FXML
    private TableColumn<Mail, String> inSenderColumn;

    @FXML
    private TableColumn<Mail, String> inSubjectColumn;

    @FXML
    private TableColumn<Mail, String> inDateColumn;

    @FXML
    private TableView<Mail> outTable;

    @FXML
    private TableColumn<Mail, String> outReceiverColumn;

    @FXML
    private TableColumn<Mail, String> outSubjectColumn;

    @FXML
    private TableColumn<Mail, String> outDateColumn;


    // BUTTONS

    @FXML
    private Button btnNewMail;

    @FXML
    private Button bntDelete;

    @FXML
    private Button btnReply;


    // MISCELLANEOUS

    @FXML
    private TextField searchField;

    @FXML
    private CheckBox cbOrderByDate;

    @FXML
    private TextArea txtArea;

    @FXML
    private ImageView imgInbox;


    // VARIABLES
    private Stage topStage;

    public Mail selectedMail;
    private String username;
    LoginController loginController = new LoginController();

    private User userModel;
    private ExecutorService mailUpdater;
    private ExecutorService emailUpdater;

    private ClientController cc;
    private final Object lock = new Object();

    public void setClientMain(LoginController loginController, User userModel, ClientController cc) {
        this.loginController = loginController;
        this.userModel = userModel;
        this.username = this.userModel.getUsername();
        this.cc = cc;
        this.updateAllEmails();
        startMailUpdater();
    }

    public void setTopStage(Stage topStage) {
        this.topStage = topStage;
    }

    private void startMailUpdater() {
        mailUpdater = Executors.newSingleThreadExecutor();
        mailUpdater.execute(() -> {
            while (true) {
                try {
                    Platform.runLater(this::updateInbox);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateOutboxEmails(Email newEmail) {
        this.updateOutbox();
        this.updateInbox();
    }

    private void updateInboxEmails() {
        inTable.getItems().clear();

        this.userModel.getInbox().stream().forEach((inboxEmail) -> {
            emailUpdater.submit(() -> {
                String receivers = inboxEmail.getTo().stream().map(Object::toString).collect(Collectors.joining("; "));
                Mail m = new Mail(inboxEmail.getId(), inboxEmail.getFrom(), inboxEmail.getSubject(), receivers, inboxEmail.getDate(),
                        inboxEmail.getSubject());
                synchronized (lock) {
                    Platform.runLater(() -> inTable.getItems().add(m));
                }
            });
        });
    }

    private void updateOutboxEmails() {
        outTable.getItems().clear();

        this.userModel.getOutbox().stream().forEach((outboxEmail) -> {
            emailUpdater.submit(() -> {
                String receivers = outboxEmail.getTo().stream().map(Object::toString).collect(Collectors.joining("; "));

                Mail m = new Mail(outboxEmail.getId(), outboxEmail.getFrom(), outboxEmail.getSubject(), receivers, outboxEmail.getDate(),
                        outboxEmail.getSubject());
                synchronized (lock) {
                    Platform.runLater(() -> outTable.getItems().add(m));
                }
            });
        });
    }

    public void updateAllEmails() {

        emailUpdater = Executors.newFixedThreadPool(10);

        this.updateInbox();
        this.updateOutbox();
    }

    public void updateInbox() {
        int size = this.cc.requestInbox();
        if(size == -1)
            System.out.println("[MailBoxController] Error requesting inbox");
        if(size > 0)
            this.updateInboxEmails();
    }
    public void updateOutbox() {
        int size = this.cc.requestOutbox();
        if(size == -1)
            System.out.println("Error requesting outbox");
        if(size > 0)
            this.updateOutboxEmails();
    }

    @FXML
    private void initialize(){
        // Set up the columns in the inbox table
        inSenderColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));
        inSubjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        inDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        // Set up the columns in the outbox table
        outReceiverColumn.setCellValueFactory(new PropertyValueFactory<>("receiversString"));
        outSubjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        outDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        // Set up the selection listeners for the inbox and outbox tables
        inTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showMailDetails(newSelection);
                this.selectedMail = newSelection;
            }
        });

        outTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showMailDetails(newSelection);
                this.selectedMail = newSelection;
            }
        });
    }

    private void showMailDetails(Mail mail) {
        lblSubject.setText(mail.getSubject());
        lblFromVuota.setText(mail.getSender());
        lblDateVuota.setText(mail.getFormattedDate());
        lblToVuota.setText("" + mail.getReceivers());
        txtArea.setText(mail.getMessage());
        btnReply.setDisable(false);
        bntDelete.setDisable(false);
    }

    @FXML
    public void reply() {
        loginController.showSendMailDialog(new Mail(this.selectedMail.getId(),
                        this.userModel.getUsername(),
                        "[RE]" + selectedMail.getSubject(),
                        selectedMail.getSender().toString(),
                        LocalDateTime.now(),
                        "\n---\n" + selectedMail.getSender() + ":\n\n" + selectedMail.getMessage()),
                "Reply Email");
    }

    @FXML
    public void delete() {
        boolean success = cc.deleteMail(selectedMail);
        showMailDetails(new Mail("",
                "",
                "",
                "",
                LocalDateTime.now(),
                ""));
        if(success) {
            try {
                ArrayList<String> receivers = new ArrayList<>();
                for (String receiver : selectedMail.getReceivers())
                    receivers.add(receiver);
                userModel.removeFromInbox(new Email(selectedMail.getId(), selectedMail.getSender(), receivers, selectedMail.getSubject(), selectedMail.getMessage(), selectedMail.getDate()));
                this.updateInboxEmails();
            } catch (Exception xcpt) {

            }
            try {
                ArrayList<String> receivers = new ArrayList<>();
                for (String receiver : selectedMail.getReceivers())
                    receivers.add(receiver);
                userModel.removeFromOutbox(new Email(selectedMail.getId(), selectedMail.getSender(), receivers, selectedMail.getSubject(), selectedMail.getMessage(), selectedMail.getDate()));
                this.updateOutboxEmails();
            } catch (Exception xcpt) {

            }
        }
    }

    @FXML
    private void handleNew() {
        loginController.showSendMailDialog(new Mail("",
                username,
                "",
                null,
                LocalDateTime.now(),
                ""), "Send new email");
    }

}
