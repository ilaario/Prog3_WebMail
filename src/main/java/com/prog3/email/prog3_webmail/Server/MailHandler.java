package com.prog3.email.prog3_webmail.Server;

import com.prog3.email.prog3_webmail.Utilities.Email;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MailHandler {
    public MailHandler() {

    }

    public synchronized boolean save(Email email) {
        try {
            String from = email.getFrom();
            List<String> to = email.getTo();

            System.out.println("[MailHandler] Saving email from " + from + " to " + to);
            File senderDir = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + from + "/out/");
            if(!senderDir.exists()) {
                senderDir.mkdirs();
            }
            File emailFile = new File(senderDir, email.getId() + " - " + email.getDate() + ".txt");
            System.out.println("[MailHandler] Email file: " + emailFile);

            ObjectOutputStream fileOutputStream = new ObjectOutputStream(new FileOutputStream(emailFile));

            fileOutputStream.writeObject(email);
            fileOutputStream.close();

            for (String r : to){
                File receiverDir = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + r + "/in/");
                if(!receiverDir.exists()) {
                    receiverDir.mkdirs();
                }
                emailFile = new File(receiverDir, email.getId() + " - " + email.getDate() + ".txt");
                System.out.println("[MailHandler] Email file: " + emailFile);

                fileOutputStream = new ObjectOutputStream(new FileOutputStream(emailFile));

                fileOutputStream.writeObject(email);

                fileOutputStream.close();
            }
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void delete(String user, Email data) {
        try {
            Files.delete(Paths.get("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles" + user + "/in/" + data.getId() + " - " + data.getDate() + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.delete(Paths.get("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles" + user + "/out/" + data.getId() + " - " + data.getDate() + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<Email> loadInBox(String user) {
        ArrayList<Email> allEmails = new ArrayList<>();
        File dir = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/in");

        System.out.println("[MailHandler] Loading inbox for " + user);

        if (dir.exists() && dir.isDirectory()) {
            //try {
            for (File textFile : Objects.requireNonNull(dir.listFiles())) {
                try (ObjectInputStream fileInputStream = new ObjectInputStream(new FileInputStream(textFile))) {
                    Email email = (Email) fileInputStream.readObject();
                    allEmails.add(email);

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("[MailHandler] Inbox loaded for " + user + ": " + allEmails.size() + " emails");
        return allEmails;
    }

    public synchronized ArrayList<Email> loadOutBox(String user) {

        ArrayList<Email> out = new ArrayList<>();

        File dir = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/out");

        if (dir.exists() && dir.isDirectory()) {

            for (File textFile : Objects.requireNonNull(dir.listFiles())) {
                try (ObjectInputStream fileInputStream = new ObjectInputStream(new FileInputStream(textFile))) {
                    Email email = (Email) fileInputStream.readObject();
                    out.add(email);

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return out;
    }
}
