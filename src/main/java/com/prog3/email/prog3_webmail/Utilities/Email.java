package com.prog3.email.prog3_webmail.Utilities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Email implements Serializable {
    private String id;
    private String from;
    private ArrayList<String> to;
    private String subject;
    private String body;
    private LocalDateTime date;
    private boolean read;
    private boolean deleted;

    public Email(String from, ArrayList<String> to, String subject, String body, LocalDateTime date) {
        this.id = UUID.randomUUID().toString();
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.date = date;
    }

    public Email(String id, String from, ArrayList<String> to, String subject, String body, LocalDateTime date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public ArrayList<String> getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setRead(){
        this.read = true;
    }

    public void setDeleted(){
        this.deleted = true;
    }

    @Override
    public String toString() {
        return "Email{" +
                "id='" + id + '\'' +
                ", from='" + from + '\'' +
                ", to=" + to +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", date=" + date +
                ", read=" + read +
                ", deleted=" + deleted +
                '}';
    }

    public boolean equals(Email email){
        if(this.id.equals(email.getId())) {
            return true;
        } else {
            if(this == email)
                return true;
            if(email == null)
                return false;
            if(getClass() != email.getClass())
                return false;
        }
        return false;
    }

    public int hashCode(){
        return this.id.hashCode();
    }
}
