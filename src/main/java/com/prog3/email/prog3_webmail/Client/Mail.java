package com.prog3.email.prog3_webmail.Client;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mail implements Serializable {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9_.+-]+\\.[a-zA-Z0-9-.]+");
    private String id;
    private StringProperty from;
    private StringProperty subject;
    private ListProperty<String> to;
    private ObjectProperty<LocalDateTime> date;
    private StringProperty message;
    private BooleanProperty isSent;

    public Mail(String id, String sender, String subject, String receivers, LocalDateTime localDateTime, String message) {
        this.id = id;
        this.from = new SimpleStringProperty(sender);
        this.subject = new SimpleStringProperty(subject);
        this.to = new SimpleListProperty<>();
        if (receivers != null)
            setReceivers(receivers);
        this.date = new SimpleObjectProperty<>(localDateTime);
        this.message = new SimpleStringProperty(message);
        this.isSent = new SimpleBooleanProperty(false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String setSender() {
        return from.get();
    }

    public StringProperty senderProperty() {
        return from;
    }

    public void setSender(String sender) {
        this.from.set(sender);
    }

    public String getSubject() {
        return subject.get();
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public ObservableList<String> getReceivers() {
        return to.get();
    }

    private void setReceivers(String receivers) {
        ArrayList<String> receiversList = new ArrayList<>();

        String[] receiversArray = receivers.split(",");
        for (String receiver : receiversArray) {
            String trimmedReceiver = receiver.trim();
            if (isValidEmail(trimmedReceiver))
                receiversList.add(trimmedReceiver);
        }

        to.set(FXCollections.observableArrayList(receiversList));
    }

    private boolean isValidEmail(String trimmedReceiver) {
        Matcher m = VALID_EMAIL_ADDRESS_REGEX.matcher(trimmedReceiver);
        return m.find(); //maybe use m.matches() instead
    }

    public StringProperty toStringProperty(){
        if(to == null){
            return new SimpleStringProperty("");
        }
        StringBuilder sb = new StringBuilder();
        for(String s : to){
            sb.append(s);
            sb.append(", ");
        }
        return new SimpleStringProperty(sb.toString());
    }

    public String getToStringProperty(){
        if(to == null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String s : to){
            sb.append(s);
            sb.append(", ");
        }
        return sb.toString();
    }

    public ObjectProperty<LocalDateTime> dateProperty() {
        return date;
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public synchronized String getFormattedDate() {
        return date.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public void setDate(LocalDateTime date) {
        this.date.set(date);
    }

    public String getMessage() {
        return message.get();
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public BooleanProperty isSentProperty() {
        return isSent;
    }

    public boolean isSent() {
        return isSent.get();
    }

    public void setSent(boolean sent) {
        isSent.set(sent);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("- From: ").append(from).append("\n");
        builder.append("- Subject: ").append(subject).append("\n");
        builder.append("- To: ").append(to).append("\n");
        if (date != null) {
            builder.append("- Date: ").append(getFormattedDate());
        } else {
            builder.append("- Date: null");
        }
        return builder.toString();
    }

    public String getSender() {
        return from.get();
    }
}
