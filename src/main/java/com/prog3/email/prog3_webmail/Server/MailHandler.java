package com.prog3.email.prog3_webmail.Server;

import com.prog3.email.prog3_webmail.Utilities.Email;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
            File emailFile = new File(senderDir, email.getDate() + ".txt");
            System.out.println("[MailHandler] Email file: " + emailFile);

            ObjectOutputStream fileOutputStream = new ObjectOutputStream(new FileOutputStream(emailFile));

            fileOutputStream.writeObject(email);
            fileOutputStream.close();

            for (String r : to){
                File receiverDir = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + r + "/in/");
                if(!receiverDir.exists()) {
                    receiverDir.mkdirs();
                }
                emailFile = new File(receiverDir, email.getDate() + ".txt");
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

    public synchronized void delete(String user, Email data) {
        try {
            System.out.println("[MailHandler] Deleting email from " + user + " inbox (" + data.getDate() + ".txt" + ")");
            Files.delete(Paths.get(
                    "/Users/ilaario/Desktop/Prog III/Prog3_WebMail/src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/in/" + data.getDate() + ".txt"));
            System.out.println("[MailHandler] Deleted email from " + user + " inbox (" + data.getDate() + ".txt" + ")");
        } catch (NoSuchFileException e) {
            System.err.println("[MailHandler] Inbox - No such file or directory");
        } catch (IOException e) {
            System.err.println("[MailHandler] Inbox - Invalid permissions");
        } catch (SecurityException e) {
            System.err.println("[MailHandler] Inbox - Not enough permissions");
        }

        try {
            Files.delete(Paths.get(
                    "/Users/ilaario/Desktop/Prog III/Prog3_WebMail/src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/" + user + "/out/" +  data.getDate() + ".txt"));
            System.out.println("[MailHandler] Deleted email from " + user + " outbox (" + data.getId() + " - " +  data.getDate() + ".txt" + ")");
        } catch (NoSuchFileException e) {
            System.err.println("[MailHandler] Outbox - No such file or directory");
        } catch (IOException e) {
            System.err.println("[MailHandler] Outbox - Invalid permissions");
        } catch (SecurityException e) {
            System.err.println("[MailHandler] Outbox - Not enough permissions");
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

        System.out.println("[MailHandler] Loading outbox for " + user);

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

        System.out.println("[MailHandler] Outbox loaded for " + user + ": " + out.size() + " emails");
        return out;
    }
}
