package com.prog3.email.prog3_webmail.Client;

import com.prog3.email.prog3_webmail.Utilities.Email;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private ListView<String> inTab;

    @FXML
    private ListView<String> outTab;

    // BUTTONS

    @FXML
    private Button btnNewMail;

    @FXML
    private Button btnDelete;

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


    private final Path userFilesPath = Paths.get("src", "main", "java", "com", "prog3", "email", "prog3_webmail", "Server", "UsersFiles");


    // VARIABLES
    private Stage topStage;

    public String selectedMail;
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
        this.hideMailDetails();
        startMailUpdater();
    }

    public void setTopStage(Stage topStage) {
        this.topStage = topStage;
    }

    private void hideMailDetails() {
        lblSubject.setText("");
        lblFromVuota.setText("");
        lblDateVuota.setText("");
        lblToVuota.setText("");
        txtArea.setText("");
        btnReply.setVisible(false);
        btnDelete.setVisible(false);
        txtArea.setVisible(false);
        lblSubject.setVisible(false);
        lblFrom.setVisible(false);
        lblTo.setVisible(false);
        lblDate.setVisible(false);
        lblSubjectVuota.setVisible(false);
        lblFromVuota.setVisible(false);
        lblToVuota.setVisible(false);
        lblDateVuota.setVisible(false);
        searchField.setVisible(true);
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
        sortListViewByDate(outTab);
        this.updateInbox();
        sortListViewByDate(inTab);

    }

    private void updateInboxEmails() {
        inTab.getItems().clear();

        this.userModel.getInbox().stream().forEach((inboxEmail) -> {
            emailUpdater.submit(() -> {
                String receivers = inboxEmail.getReceivers().stream().map(Object::toString).collect(Collectors.joining("; "));
                Mail m = new Mail(inboxEmail.getId(), inboxEmail.getSender(), inboxEmail.getSubject(), receivers, inboxEmail.getTimestamp(),
                        inboxEmail.getSubject());
                synchronized (lock) {
                    Platform.runLater(() -> {
                        if (inTab.getItems().isEmpty()) {
                            inTab.getItems().add(writeInMail(m.getDate().toString()));
                            sortListViewByDate(inTab);

                            return;
                        }
                        synchronized (lock){
                            List<String> mailList = Collections.synchronizedList(inTab.getItems());
                            Iterator<String> iterator = mailList.iterator();
                            inTab.getItems().add(writeInMail(m.getDate().toString()));
                            sortListViewByDate(inTab);
                        }
                    });
                }
            });
        });
    }

    private void updateOutboxEmails() {
        outTab.getItems().clear();

        this.userModel.getOutbox().stream().forEach((outboxEmail) -> {
            emailUpdater.submit(() -> {
                String receivers = outboxEmail.getReceivers().stream().map(Object::toString).collect(Collectors.joining("; "));
                Mail m = new Mail(outboxEmail.getId(), outboxEmail.getSender(), outboxEmail.getSubject(), receivers, outboxEmail.getTimestamp(),
                        outboxEmail.getSubject());
                synchronized (lock) {
                    Platform.runLater(() -> {
                        if (outTab.getItems().isEmpty()) {
                            outTab.getItems().add(outTab.getItems().size(), writeOutMail(m.getDate().toString()));
                            sortListViewByDate(outTab);
                            return;
                        }

                        synchronized (lock){
                            List<String> mailList = Collections.synchronizedList(outTab.getItems());
                            Iterator<String> iterator = mailList.iterator();
                            outTab.getItems().add(outTab.getItems().size(), writeOutMail(m.getDate().toString()));
                            sortListViewByDate(outTab);
                        }
                    });
                }
            });
        });
    }

    public static void sortListViewByDate(ListView<String> listView) {
        ObservableList<String> items = listView.getItems();

        System.out.println("[MailBoxController] sortListViewByDate: " + items);

        // Creazione di un comparatore basato sulle date
        Comparator<String> dateComparator = Comparator.comparing(MailBoxController::extractDateFromString);

        // Ordina la lista usando il comparatore
        items.sort(dateComparator);

        // Aggiorna la ListView
        listView.setItems(items);
    }

    private static Date extractDateFromString(String input) {
        // Cerca la data utilizzando un'espressione regolare
        Pattern pattern = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}\\b");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // Se trova la corrispondenza, converte la stringa della data in un oggetto Date
            String dateString = matcher.group();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace(); // Gestire l'eccezione in base alle tue esigenze
                return null;
            }
        } else {
            // Se non trova una corrispondenza, gestisce la situazione di default
            return null;
        }
    }

    @NotNull
    private synchronized String writeOutMail(@NotNull String m) {
        /* File file = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + this.username + "/out/" + m.getDate() + ".json");
        System.out.println("[MailBoxController] writeOutMail: " + file);
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            String JSON_string = "";
            while(line != null)
            {
                System.out.println(line);
                JSON_string += line;
                line = in.readLine();
            }
            in.close();

            // Crea un oggetto JSON
            JSONObject jsonObject = new JSONObject(JSON_string);

            // Estrai i campi desiderati
            String oggetto = jsonObject.getString("oggetto");
            String timestamp = jsonObject.getString("timestamp");
            String destinatario = jsonObject.getJSONArray("destinatario").toString();
            String regex = "[\\[\\]\"']"; // Rimuove [ ] " '

            destinatario = destinatario.replaceAll(regex, "");


            // Stampi i campi desiderati
            StringBuilder sb = new StringBuilder();
            sb.append(destinatario + "\n");
            sb.append(oggetto+ "\n");
            sb.append(timestamp+ "\n");
            System.out.println("[MailBoxController] writeOutMail: " + sb.toString());
            return sb.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null; */
        return this.cc.requestOutboxMailString(m);

    }

    @NotNull
    private synchronized String writeInMail(@NotNull String m) {
        /* File file = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + this.username + "/in/" + m.getDate() + ".json");
        System.out.println("[MailBoxController] writeInMail: " + file);
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            String JSON_string = "";
            while(line != null)
            {
                System.out.println(line);
                JSON_string += line;
                line = in.readLine();
            }
            in.close();

            // Crea un oggetto JSON
            JSONObject jsonObject = new JSONObject(JSON_string);

            // Estrai i campi desiderati
            String oggetto = jsonObject.getString("oggetto");
            String timestamp = jsonObject.getString("timestamp");
            String mittente = jsonObject.getString("mittente");


            // Stampi i campi desiderati
            StringBuilder sb = new StringBuilder();
            sb.append(mittente);
            sb.append(oggetto);
            sb.append(timestamp);
            return sb.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null; */
        return this.cc.requestInboxMailString(m);
    }

    @NotNull
    private synchronized String writeMail(@NotNull Mail m) {
        /* File file = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + this.username + "/in/" + m.getDate() + ".json");
        try {
            // Crea un oggetto JSON
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            String JSON_string = "";
            while(line != null)
            {
                System.out.println(line);
                JSON_string += line;
                line = in.readLine();
            }
            in.close();

            // Crea un oggetto JSON
            JSONObject jsonObject = new JSONObject(JSON_string);

            // Estrai i campi desiderati
            String oggetto = jsonObject.getString("oggetto");
            String timestamp = jsonObject.getString("timestamp");
            String mittente = jsonObject.getString("mittente");


            // Stampi i campi desiderati
            StringBuilder sb = new StringBuilder();
            sb.append(mittente);
            sb.append(oggetto);
            sb.append(timestamp);
            return sb.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null; */
        Path filePath = userFilesPath.resolve(username).resolve("in").resolve(m.getDate() + ".json");
        return readAndParseMailFile(filePath);
    }

    @NotNull
    private String readAndParseMailFile(Path filePath) {
        try (BufferedReader in = Files.newBufferedReader(filePath)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(correggiFormatoRiga(line));
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            // Estrai i campi desiderati e restituisci il risultato
            return extractMailFields(jsonObject);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private String extractMailFields(@NotNull JSONObject jsonObject) {
        // Estrai i campi desiderati
        String oggetto = jsonObject.getString("oggetto");
        String timestamp = jsonObject.getString("timestamp");

        Object destinatarioObject = jsonObject.get("destinatario");
        String destinatario;

        if (destinatarioObject instanceof JSONArray) {
            JSONArray destinatarioArray = (JSONArray) destinatarioObject;
            destinatario = destinatarioArray.toString();
        } else {
            destinatario = destinatarioObject.toString();
        }

        String regex = "[\\[\\]\"\n']"; // Rimuove [ ] " '
        destinatario = destinatario.replaceAll(regex, "");
        oggetto = oggetto.replaceAll(regex, "");
        timestamp = timestamp.replaceAll(regex, "");

        // Stampi i campi desiderati
        StringBuilder sb = new StringBuilder();
        sb.append(destinatario).append("\n");
        sb.append(oggetto).append("\n");
        sb.append(timestamp).append("\n");

        System.out.println("[MailBoxController] writeOutMail: " + sb.toString());
        return sb.toString();
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

        // Set up the selection listeners for the inbox and outbox tables
        inTab.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    showInboxMailDetails(newSelection);
                    outTab.getSelectionModel().clearSelection(); // Clear selection in outTab
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.selectedMail = newSelection;
            }
        });

        outTab.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    showOutboxMailDetails(newSelection);
                    inTab.getSelectionModel().clearSelection(); // Clear selection in inTab
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.selectedMail = newSelection;
            }
        });

        searchField.setVisible(true);
        cbOrderByDate.setVisible(true);
    }

    @NotNull
    @Contract("_ -> new")
    private Mail StringToMail(@NotNull String mailString) throws IOException {
        String[] lines = mailString.split("\\n");

        List<String> interessanti = Arrays.stream(lines)
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();

        if (interessanti.size() < 3) {
            throw new IllegalArgumentException("La stringa di input deve contenere almeno tre righe interessanti.");
        }

        String mittente = correggiFormatoRiga(interessanti.get(0));
        String oggetto = correggiFormatoRiga(interessanti.get(1));
        String timestampString = interessanti.get(2);

        // Aggiungi stampe di debug per identificare il problema
        System.out.println("Mittente: " + mittente);
        System.out.println("Oggetto: " + oggetto);
        System.out.println("Timestamp: " + timestampString);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);

            return new Mail("", mittente, oggetto, username, timestamp, "");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Contract("_ -> new")
    private Mail InboxStringToMail(@NotNull String mailString) throws IOException {
        String[] lines = mailString.split("\\n");

        List<String> interessanti = Arrays.stream(lines)
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();

        if (interessanti.size() < 3) {
            throw new IllegalArgumentException("La stringa di input deve contenere almeno tre righe interessanti.");
        }

        String mittente = correggiFormatoRiga(interessanti.get(0));
        String oggetto = correggiFormatoRiga(interessanti.get(1));
        String timestampString = interessanti.get(2);

        // Aggiungi stampe di debug per identificare il problema
        System.out.println("Mittente: " + mittente);
        System.out.println("Oggetto: " + oggetto);
        System.out.println("Timestamp: " + timestampString);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);

            String stringMail = this.cc.requestInboxTextMailString(timestamp.toString());
            return new Mail("", mittente, oggetto, username, timestamp, stringMail);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Contract("_ -> new")
    private Mail OutboxStringToMail(@NotNull String mailString) throws IOException {
        String[] lines = mailString.split("\\n");

        List<String> interessanti = Arrays.stream(lines)
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();

        if (interessanti.size() < 3) {
            throw new IllegalArgumentException("La stringa di input deve contenere almeno tre righe interessanti.");
        }

        String mittente = correggiFormatoRiga(interessanti.get(0));
        String oggetto = correggiFormatoRiga(interessanti.get(1));
        String timestampString = interessanti.get(2);

        // Aggiungi stampe di debug per identificare il problema
        System.out.println("Mittente: " + mittente);
        System.out.println("Oggetto: " + oggetto);
        System.out.println("Timestamp: " + timestampString);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);

            String stringMail = this.cc.requestOutboxTextMailString(timestamp.toString());
            return new Mail("", mittente, oggetto, username, timestamp, stringMail);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private String correggiFormatoRiga(String riga) {
        // Rimuove eventuali \n in eccesso
        riga = riga.replaceAll("\\n+", "");

        // Aggiunge un solo \n alla fine della riga se non è già presente
        if (!riga.endsWith("\n")) {
            riga += "\n";
        }

        return riga;
    }

    @NotNull
    @Contract("_ -> new")
    private JSONObject readJsonFromFile(Path filePath) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(filePath)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        }
    }

    private void showInboxMailDetails(String mailString) throws IOException{
        txtArea.setVisible(true);
        lblSubject.setVisible(true);
        lblFrom.setVisible(true);
        lblTo.setVisible(true);
        lblDate.setVisible(true);
        lblSubjectVuota.setVisible(true);
        lblFromVuota.setVisible(true);
        lblToVuota.setVisible(true);
        lblDateVuota.setVisible(true);
        imgInbox.setVisible(false);

        Mail mail = InboxStringToMail(mailString);

        lblSubject.setText(correggiFormatoRiga(mail.getSubject()));
        lblFromVuota.setText(correggiFormatoRiga(mail.getSender()));
        lblDateVuota.setText(correggiFormatoRiga(mail.getFormattedDate()));
        lblToVuota.setText("" + mail.getReceivers());
        txtArea.setText(mail.getMessage());
        btnReply.setVisible(true);
        btnReply.setDisable(false);
        btnDelete.setVisible(true);
        btnDelete.setDisable(false);
    }

    private void showOutboxMailDetails(String mailString) throws IOException{
        txtArea.setVisible(true);
        lblSubject.setVisible(true);
        lblFrom.setVisible(true);
        lblTo.setVisible(true);
        lblDate.setVisible(true);
        lblSubjectVuota.setVisible(true);
        lblFromVuota.setVisible(true);
        lblToVuota.setVisible(true);
        lblDateVuota.setVisible(true);
        imgInbox.setVisible(false);

        Mail mail = OutboxStringToMail(mailString);

        lblSubject.setText(correggiFormatoRiga(mail.getSubject()));
        lblFromVuota.setText(correggiFormatoRiga(mail.getSender()));
        lblDateVuota.setText(correggiFormatoRiga(mail.getFormattedDate()));
        lblToVuota.setText("" + mail.getReceivers());
        txtArea.setText(mail.getMessage());
        btnReply.setVisible(true);
        btnReply.setDisable(false);
        btnDelete.setVisible(true);
        btnDelete.setDisable(false);
    }

    private void showMailDetails(String mailString) throws IOException {
        txtArea.setVisible(true);
        lblSubject.setVisible(true);
        lblFrom.setVisible(true);
        lblTo.setVisible(true);
        lblDate.setVisible(true);
        lblSubjectVuota.setVisible(true);
        lblFromVuota.setVisible(true);
        lblToVuota.setVisible(true);
        lblDateVuota.setVisible(true);
        imgInbox.setVisible(false);

        Mail mail = StringToMail(mailString);

        lblSubject.setText(correggiFormatoRiga(mail.getSubject()));
        lblFromVuota.setText(correggiFormatoRiga(mail.getSender()));
        lblDateVuota.setText(correggiFormatoRiga(mail.getFormattedDate()));
        lblToVuota.setText("" + mail.getReceivers());
        txtArea.setText(mail.getMessage());
        btnReply.setVisible(true);
        btnReply.setDisable(false);
        btnDelete.setVisible(true);
        btnDelete.setDisable(false);
    }

    @FXML
    public void reply() throws IOException {
        Mail mail = StringToMail(selectedMail);
        loginController.showSendMailDialog(new Mail(mail.getId(),
                        this.userModel.getUsername(),
                        "[RE]" + mail.getSubject(),
                        mail.getSender().toString(),
                        LocalDateTime.now(),
                        "\n---\n" + mail.getSender() + ":\n\n" + mail.getMessage()),
                "Reply Email");
    }

    @FXML
    public void delete() throws IOException {
        Mail mail = StringToMail(selectedMail);
        boolean success = cc.deleteMail(mail);
        hideMailDetails();
        imgInbox.setVisible(true);
        if(success) {
            try {
                ArrayList<String> receivers = new ArrayList<>();
                for (String receiver : mail.getReceivers())
                    receivers.add(receiver);
                userModel.removeFromInbox(new Email(mail.getId(), mail.getSender(), receivers, mail.getSubject(), mail.getMessage(), mail.getDate()));
                this.updateInboxEmails();
            } catch (Exception xcpt) {
                System.err.println("[MailBoxController] Couldn't delete mail from inbox");
            }
            try {
                ArrayList<String> receivers = new ArrayList<>();
                for (String receiver : mail.getReceivers())
                    receivers.add(receiver);
                userModel.removeFromOutbox(new Email(mail.getId(), mail.getSender(), receivers, mail.getSubject(), mail.getMessage(), mail.getDate()));
                this.updateOutboxEmails();
            } catch (Exception xcpt) {
                System.err.println("[MailBoxController] Couldn't delete mail from outbox");
            }
        } else {
            System.out.println("[MailBoxController] Error deleting mail");
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
