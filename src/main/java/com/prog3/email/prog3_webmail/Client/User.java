package com.prog3.email.prog3_webmail.Client;

import com.prog3.email.prog3_webmail.Utilities.Email;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private List<Email> inbox;
    private List<Email> outbox;

    public User(String username) {
        this.username = username;
        this.inbox = FXCollections.observableList(new ArrayList<>());
        this.outbox = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Email> getInbox() {
        ArrayList<Email> inbox= new ArrayList<>(this.inbox);
        return inbox;
    }

    public void addToInbox(List<Email> inbox) {
        this.inbox.addAll(inbox);
    }

    public List<Email> getOutbox() {
        return outbox;
    }

    public void addToOutbox(List<Email> outbox) {
        this.outbox.addAll(outbox);
    }

    public void removeFromInbox(Email email) {
        inbox.remove(email);
    }

    public void removeFromOutbox(Email email) {
        outbox.remove(email);
    }
}
