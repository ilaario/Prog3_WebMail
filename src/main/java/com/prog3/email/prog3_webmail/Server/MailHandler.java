package com.prog3.email.prog3_webmail.Server;

import com.prog3.email.prog3_webmail.Utilities.Email;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.*;

public class MailHandler {
    public MailHandler() {

    }

    public synchronized boolean save(Email mail) throws IOException {
        String sender = mail.getSender();
        List<String> receivers = mail.getReceivers();

        System.out.println("[MailHandler] sender: " + mail.getSender());

        File senderDir = new File("./src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + sender + "/out/");
        senderDir.mkdirs(); // Create directories recursively if they don't exist

        JSONObject json = new JSONObject();

        json.put("id", mail.getId());
        json.put("mittente", mail.getSender());
        json.put("destinatario", mail.getReceivers());
        json.put("oggetto", mail.getSubject());
        json.put("testo", mail.getText());

        // Controlla se la data è nulla
        if (mail.getTimestamp() != null) {
            json.put("timestamp", mail.getTimestamp());
        } else {
            json.put("timestamp", JSONObject.NULL);
        }

        File file = new File(senderDir, mail.getTimestamp() + ".json");
        System.out.println("[save] file: " + file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(json.toString());
        }

        for (String r : receivers) {
            File receiverDir = new File("./src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + r + "/in/");
            System.out.println("[save] receiverDir: " + receiverDir);
            receiverDir.mkdirs();

            json = new JSONObject();

            json.put("id", mail.getId() + "\n");
            json.put("mittente", mail.getSender() + "\n");
            json.put("destinatario", mail.getReceivers() + "\n");
            json.put("oggetto", mail.getSubject() + "\n");
            json.put("testo", mail.getText() + "\n");

            // Controlla se la data è nulla
            if (mail.getTimestamp() != null) {
                json.put("timestamp", mail.getTimestamp() + "\n");
            } else {
                json.put("timestamp", JSONObject.NULL + "\n");
            }


            File recfile = new File(receiverDir, mail.getTimestamp() + ".json");
            System.out.println("[save] file: " + recfile);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(recfile))) {
                writer.write(json.toString());
            }
        }
        return true;
    }

    public synchronized void delete(String user,Email mail) {
        try {
            Files.delete(Paths.get(
                    "src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/in/" + mail.getTimestamp() + ".json"));
        } catch (Exception e) {
            System.out.println("Can't delete in");
        }
        try {
            Files.delete(Paths.get(
                    "src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/out/" + mail.getTimestamp() + ".json"));
        } catch (Exception e) {
            System.out.println("Can't delete out");
        }
    }

    public synchronized ArrayList<Email> loadInBox(String user) throws FileNotFoundException {
        ArrayList<Email> allEmails = new ArrayList<>();
        File dir = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/in");

        if (dir.exists() && dir.isDirectory()) {
            //try {
            for (File textFile : Objects.requireNonNull(dir.listFiles())) {
                JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(textFile)));
                String id = jsonObject.getString("id");
                String mittente = jsonObject.getString("mittente");
                ArrayList<String> destinatario;
                if (jsonObject.get("destinatario") instanceof JSONArray) {
                    destinatario = jsonArrayToArrayList(jsonObject.getJSONArray("destinatario"));
                } else {
                    destinatario = new ArrayList<>();
                    destinatario.add(jsonObject.getString("destinatario"));
                }                String oggetto = jsonObject.getString("oggetto");
                String testo = jsonObject.getString("testo");
                String timestampString = jsonObject.getString("timestamp");
                System.out.println("[loadInBox] timestampString: '" + timestampString + "'");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                String trimmedTimestampString = timestampString.trim(); // Rimuovi spazi bianchi
                System.out.println("[loadInBox] trimmedTimestampString: '" + trimmedTimestampString + "'");
                LocalDateTime timestamp = LocalDateTime.parse(trimmedTimestampString, formatter);
                System.out.println("[loadInBox] timestamp: '" + timestamp + "'");
                Email email = new Email(id, mittente, destinatario, oggetto, testo, timestamp);
                allEmails.add(email);
            }
        }

        return allEmails;
    }

    public synchronized ArrayList<Email> loadOutBox(String user) throws FileNotFoundException {

        ArrayList<Email> out = new ArrayList<>();

        File dir = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/out");

        if (dir.exists() && dir.isDirectory()) {

            for (File textFile : Objects.requireNonNull(dir.listFiles())) {
                JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(textFile)));
                String id = jsonObject.getString("id");
                String mittente = jsonObject.getString("mittente");
                ArrayList<String> destinatario;
                if (jsonObject.get("destinatario") instanceof JSONArray) {
                    destinatario = jsonArrayToArrayList(jsonObject.getJSONArray("destinatario"));
                } else {
                    destinatario = new ArrayList<>();
                    destinatario.add(jsonObject.getString("destinatario"));
                }
                String oggetto = jsonObject.getString("oggetto");
                String testo = jsonObject.getString("testo");
                String timestampString = jsonObject.getString("timestamp");
                System.out.println("[loadOutBox] timestampString: '" + timestampString + "'");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                String trimmedTimestampString = timestampString.trim(); // Rimuovi spazi bianchi
                System.out.println("[loadOutBox] trimmedTimestampString: '" + trimmedTimestampString + "'");
                LocalDateTime timestamp = LocalDateTime.parse(trimmedTimestampString, formatter);
                System.out.println("[loadOutBox] timestamp: '" + timestamp + "'");
                Email email = new Email(id, mittente, destinatario, oggetto, testo, timestamp);
                out.add(email);
            }
        }
        return out;
    }

    private static ArrayList<String> jsonArrayToArrayList(JSONArray input) {
        ArrayList<String> output = new ArrayList<>();
        if (input != null) {
            int len = input.length();
            for (int i = 0; i < len; i++) {
                output.add(input.get(i).toString());
            }
        }
        return output;
    }
}
