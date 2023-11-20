package com.prog3.email.prog3_webmail.Server;

import com.prog3.email.prog3_webmail.Utilities.CS_Comm;
import com.prog3.email.prog3_webmail.Utilities.Email;
import com.prog3.email.prog3_webmail.Utilities.LoginResponse;
import com.prog3.email.prog3_webmail.Server.LogVerbose;
import com.prog3.email.prog3_webmail.Server.UserUtils;
import com.prog3.email.prog3_webmail.Server.UserList;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;

import org.json.*;

public class ServerHandler implements Runnable {
    private Socket incoming;
    public UserUtils userService;
    private MailHandler mailHandler;


    LogVerbose log;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;


    UserList userList = new UserList();
    public ServerHandler(Socket incoming, LogVerbose log) {
        this.incoming = incoming;
        this.log = log;
        userService = new UserUtils();
        try {
            inputStream = new ObjectInputStream(incoming.getInputStream());
            outputStream = new ObjectOutputStream(incoming.getOutputStream());
            this.mailHandler = new MailHandler();
        } catch (IOException xcpt) {
            xcpt.printStackTrace();
        }
//    System.out.println("serverhandler constructor called");
    }

    public UserList getUserList() {
        File userFolder = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/");
        File[] userFolders = userFolder.listFiles();

        if (userFolders != null) {
            for (File folder : userFolders) {
                if (folder.isDirectory()) {
                    String folderName = folder.getName();
                    userList.addUser(folderName);
                }
            }
        }

        return userList;
    }

    /*
     * @brief: method run, is the first things called, so here in base of the
     * request we call the right method
     */
    @Override
    public void run() {
        try {
            try {
                UserList userList = this.getUserList();
                for(String user : userList.getUsers()){
                    log.setLog("user list is " + user);
                }
                assert userList != null;

                try {
                    CS_Comm c = (CS_Comm) inputStream.readObject();
                    log.setLog("Action registered: " + c.getCommand());
                    switch (c.getCommand()) {
                        case "login" -> handleLoginAction((String) c.getData());
                        case "inbox" -> handleInboxAction((String)((Pair) c.getData()).getKey(), (List<Email>) ((Pair) c.getData()).getValue());
                        case "send" -> handleSendAction(userList, (Email) c.getData());
                        case "delete" -> handleDeleteAction((String) ((Pair) c.getData()).getKey(), (Email) ((Pair) c.getData()).getValue());
                        case "outbox" -> handleOutboxAction((String)((Pair) c.getData()).getKey(), (List<Email>) ((Pair) c.getData()).getValue());
                        default -> log.setLog("Unrecognized action");
                    }

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (EOFException xcpt) {
//          System.out.println("NULL - end of requests");
                }

            } finally {
                log.setLog("Client disconnected");
                incoming.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteAction(String user, Email body) {
        try {

            mailHandler.delete(user,body);

            CS_Comm response = new CS_Comm("delete_ok", body);

            outputStream.writeObject(response);
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleLoginAction(String username) throws IOException, ClassNotFoundException {
        UserList userList = getUserList();
        if(!userList.userExist(username)){
            userService.createUserFolders(username);
            userList.addUser(username);
        }
        Set<String> set = userService.getUsernamesFromDirectory(username);

        log.setLog("User " + username + " logged in");

        CS_Comm c = new CS_Comm("loginRes", new LoginResponse());

        outputStream.writeObject(c);

    }

    private void handleInboxAction(String username, List<Email> userInbox) throws IOException, ClassNotFoundException {
        System.out.print("[SERVER] User connected: ");
        for (String user : userList.getUsers()) {
            System.out.print(user + ", ");
        }
        System.out.println();
        ArrayList<Email> loadedInbox = mailHandler.loadInBox(username);
        ArrayList<Email> newEmails = new ArrayList<>();
        for (Email email : loadedInbox) {
            if(!userInbox.contains(email)) {
                newEmails.add(email);
            }
        }
        CS_Comm response = new CS_Comm("inbox", newEmails);
        outputStream.writeObject(response);
    }

    private void handleOutboxAction(String username, List<Email> userOutbox) throws IOException, ClassNotFoundException {
        ArrayList<Email> loadedOutbox = mailHandler.loadOutBox(username);
        ArrayList<Email> newEmails = new ArrayList<>();
        for (Email email : loadedOutbox) {
            if(!userOutbox.contains(email)) {
                newEmails.add(email);
            }
        }
        CS_Comm response = new CS_Comm("outbox",newEmails);
        outputStream.writeObject(response);
    }

    private void handleSendAction(UserList userList, Email mail) throws IOException, ClassNotFoundException {
        Set<String> receivers = new HashSet<>(mail.getReceivers());
        for (String receiver : receivers) {
            if (!userList.userExist(receiver)) {
                System.out.println("[SERVER] User " + receiver + " does not exist");
                CS_Comm response = new CS_Comm("send_not_ok", mail);
                outputStream.writeObject(response);
                return;
            }
            log.setLog(mail.getSender() + " sent an email to " + mail.getReceivers());
            mail.setBin(true);

            if (!mailHandler.save(mail)) {
                System.out.println("[SERVER] Error while saving email");
                CS_Comm response = new CS_Comm("send_not_ok", mail);
                outputStream.writeObject(response);
                return;
            }

            CS_Comm response = new CS_Comm("send_ok", mail);
            outputStream.writeObject(response);
        }
    }

    private synchronized void closeConnection() {
        try {
            log.setLog("closing connection");
            incoming.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
