package server.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.service.AuthService;
import server.service.Manager;

public class Server {
    private class ServerWorker implements Runnable {
        Manager m;
        AuthService authService;
        Socket client_socket;
        DataInputStream iStream;
        DataOutputStream oStream;

        public ServerWorker(Socket cs, Manager m, AuthService authService) throws IOException {
            this.m = m;
            this.authService = authService;
            client_socket = cs;
            iStream = new DataInputStream(cs.getInputStream());
            oStream = new DataOutputStream(cs.getOutputStream());
        }


        public void run() {
            //need to design protocol for this one

            //authenticate first, then do the rest (handle requests and allat)
            //flag to check if authenticated?
        }
    }

    private ServerSocket s;
    private Manager m;
    private AuthService authService;

    public Server() throws IOException {
        this.m = new Manager(10);
        this.s = new ServerSocket(65432);
        this.authService = AuthService.getInstance();
    }


    public void run() throws IOException {
        while(true) {
            Socket client_socket = s.accept();
            ServerWorker w = new ServerWorker(client_socket, m, authService);
            Thread t = new Thread(w);
            t.start();
        }
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            s.run();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
