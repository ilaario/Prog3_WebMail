package com.prog3.email.prog3_webmail.Server;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class UserUtils {

    private static final String PATH = "./src/main/java/com/prog3/email/prog3_webmail/Server/UsersFiles/";

    /*
     * @brief: using Set data structure for uniqueness of elements, avoiding the
     * need to check each files name
     */
    public Set<String> getUsernamesFromDirectory(String username) {
        File directory = new File(PATH + username);
        File[] files = directory.listFiles();
        Set<String> usernames = new HashSet<>();

        assert files != null;
        for (File file : files) {
            usernames.add(file.getName());
        }

        return usernames;
    }

    public Set<String> createUserFolders(String username) {
        File directory = new File(PATH + username);
        directory.mkdir();
        File inbox = new File(PATH + username + "/in/");
        inbox.mkdir();
        File outbox = new File(PATH + username + "/out/");
        outbox.mkdir();

        return getUsernamesFromDirectory(username);

    }
}
