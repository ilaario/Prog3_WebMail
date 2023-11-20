package com.prog3.email.prog3_webmail.Utilities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/*
 * @brief: this class is the email object that is used to send emails between client and server
 * it differs with Mail class because this is the one that occur in backend, while Mail is the one that occur in frontend
 * */
public class Email implements Serializable {

    private String id;
    private String sender;
    private ArrayList<String> receivers;
    private String subject;
    private String text;
    private LocalDateTime timestamp;
    private boolean bin;

    private static final long serialVersionUID = -6704926522070994084L;

    public Email(String sender, ArrayList<String> receivers, String subject, String text, LocalDateTime timestamp) {
        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.subject = subject;
        this.text = text;
        this.receivers = new ArrayList<>(receivers);
        this.timestamp = timestamp;
    }

    public Email(String id, String sender, ArrayList<String> receivers, String subject, String text, LocalDateTime timestamp) {
        this.id = id;
        this.sender = sender;
        this.subject = subject;
        this.text = text;
        this.receivers = new ArrayList<>(receivers);
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public ArrayList<String> getReceivers() {
        return receivers;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceivers(ArrayList<String> receivers) {
        this.receivers = receivers;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getBin() {
        return bin;
    }

    public void setBin(boolean bin) {
        this.bin = bin;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("{\n");
        builder.append("  \"id\": \" " + id + "\",\n");
        builder.append("  \"mittente\": \"" + sender + "\",\n");
        builder.append("  \"destinatario\": \"" + receivers + "\",\n");
        builder.append("  \"oggetto\": \"" + subject + "\",\n");
        builder.append("  \"testo\": \"" + text + "\",\n");
        if (timestamp != null) {
            builder.append("  \"timestamp\": \"" + timestamp + "\"\n");
        } else {
            builder.append("  \"timestamp\": \"null\"\n");
        }
        builder.append("}");

        return builder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if(this == object)
            return true;
        if(object == null)
            return false;
        if(getClass() != object.getClass())
            return false;
        Email email = (Email) object;

        if(this.id.equals(email.getId()))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
