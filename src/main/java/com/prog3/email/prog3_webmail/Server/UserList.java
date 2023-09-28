package com.prog3.email.prog3_webmail.Server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.List;

public class UserList implements Serializable {
    private static ObservableList<String> users;

    public UserList(List<String> users) {
        this.users = FXCollections.observableArrayList(users);
    }

    public UserList(){
        users = FXCollections.observableArrayList();
    }

    public void addUser(String user){
        users.add(user);
    }

    public static boolean userExist(String user){
        return users.contains(user);
    }

    public ObservableList<String> getUsers() {
        return users;
    }
}
