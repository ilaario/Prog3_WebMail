package com.prog3.email.prog3_webmail.Server;

import java.io.File;
import java.util.Set;

public class UserUtils {
    private static final String PATH = "src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/";

    public Set<String> getUsernames(String username) {
        File directory = new File(PATH + username);
        File[] files = directory.listFiles();
        Set<String> usernames = new java.util.HashSet<>();

        assert files != null;
        for (File file : files) {
            usernames.add(file.getName());
        }
        return usernames;
    }

    public Set<String> handleUserLogin(String username) {
        if(UserList.userExist(username)){
            return getUsernames(username);
        } else {
            File directory = new File(PATH + username);
            if(!directory.mkdir()){
                System.err.println("Error creating user folder");
            }
            File inbox = new File(PATH + username + "/in");
            if(!inbox.mkdir()){
                System.err.println("Error creating inbox folder");
            }
            File outbox = new File(PATH + username + "/out");
            if(!outbox.mkdir()){
                System.err.println("Error creating outbox folder");
            }

            return getUsernames(username);
        }
    }
}
