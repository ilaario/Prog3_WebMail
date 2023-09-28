package com.prog3.email.prog3_webmail.Server;

import com.prog3.email.prog3_webmail.Utilities.CS_Comm;
import com.prog3.email.prog3_webmail.Utilities.LoginResponse;
import com.prog3.email.prog3_webmail.Utilities.Email;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerHandler implements Runnable {
    private Socket incoming;
    public UserUtils userUtils;
    private MailHandler mailHandler;

    LogVerbose log;
    ObjectOutputStream out;
    ObjectInputStream in;

    UserList userList = new UserList();

    public ServerHandler(Socket incoming, LogVerbose log) {
        this.incoming = incoming;
        this.log = log;
        userUtils = new UserUtils();
        try {
            in = new ObjectInputStream(incoming.getInputStream());
            out = new ObjectOutputStream(incoming.getOutputStream());
            this.mailHandler = new MailHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserList getUserList() {
        File directory = new File("src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/");
        File[] users = directory.listFiles();

        if (users != null) {
            for (File user : users) {
                if (user.isDirectory()) {
                    String username = user.getName();
                    userList.addUser(username);
                }
            }
        }
        return userList;
    }

    @Override
    public void run() {
        try {
            try {
                UserList userList = this.getUserList();
                for(String user : userList.getUsers()){
                    log.setLog("user list is " + user);
                }

                try {
                    CS_Comm comm = (CS_Comm) in.readObject();
                    log.setLog("Received: " + comm.getCommand());
                    switch (comm.getCommand()){
                        case "login":
                            handleLogin((String)comm.getData());
                            break;
                        case "send":
                            handleSend(userList, (Email)comm.getData());
                            break;
                        case "delete":
                            handleDelete((String) ((Pair) comm.getData()).getKey(), (Email) ((Pair) comm.getData()).getValue());
                            break;
                        case "inbox":
                            handleInboxAction((String)((Pair) comm.getData()).getKey(), (List<Email>) ((Pair) comm.getData()).getValue());
                            break;
                        case "outbox":
                            handleOutboxAction((String)((Pair) comm.getData()).getKey(), (List<Email>) ((Pair) comm.getData()).getValue());
                            break;
                        default:
                            log.setLog("Command not recognized by server");
                            break;
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (EOFException e){

                }
            } finally {
                log.setLog("Client disconnected");
                incoming.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handleDelete(String user, Email data) {
        try{
            mailHandler.delete(user, data);
            CS_Comm response = new CS_Comm("delete_ok", data);
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSend(UserList users, Email mail) {
        Set<String> receivers  = new HashSet<>(mail.getTo());
        for (String receiver : receivers) {
            if (!users.userExist(receiver)) {
                CS_Comm response = new CS_Comm("send_error", "User " + receiver + " does not exist");
                try {
                    out.writeObject(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            log.setLog("Sending mail to " + receiver);
            mail.setDeleted();

            if(!mailHandler.save(mail)){
                CS_Comm response = new CS_Comm("send_error", "Error while saving mail");
                try {
                    out.writeObject(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            CS_Comm response = new CS_Comm("send_ok", "Mail sent");
            try {
                out.writeObject(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLogin(String username) throws IOException, ClassNotFoundException {
        UserList userList = getUserList();
        userUtils.handleUserLogin(username);
        Set<String> set = userUtils.getUsernames(username);

        log.setLog("User logged in: " + username);
        CS_Comm comm = new CS_Comm("login_ok", new LoginResponse());

        out.writeObject(comm);
    }

    private void handleInboxAction(String username, List<Email> userInbox) throws IOException, ClassNotFoundException {
        ArrayList<Email> loadedInbox = mailHandler.loadInBox(username);
        ArrayList<Email> newEmails = new ArrayList<>();
        for (Email email : loadedInbox) {
            if(!userInbox.contains(email)) {
                newEmails.add(email);
            }
        }
        CS_Comm response = new CS_Comm("inbox", newEmails);
        out.writeObject(response);
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
        out.writeObject(response);
    }

}
