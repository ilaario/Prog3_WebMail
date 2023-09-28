package com.prog3.email.prog3_webmail.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerController implements Runnable{
    public LogVerbose log;

    public ServerController(LogVerbose log) {
        this.log = log;
    }

    @Override
    public void run() {
        try{
            ServerSocket s = new ServerSocket(8189);
            while(true){
                Socket incoming = s.accept();
                Runnable r = new ServerHandler(incoming, log);
                new Thread(r).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
