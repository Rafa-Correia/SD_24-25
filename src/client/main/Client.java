package client.main;

import client.service.Menu;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private final Menu menu;

    private final byte[] msg = new byte[10];
    


    public Client (Socket s) throws IOException {
        DataInputStream is = new DataInputStream(s.getInputStream());
        DataOutputStream os = new DataOutputStream(s.getOutputStream());
        
        this.menu = new Menu(is, os);

        for (int i = 0; i < msg.length; i++) {
            msg[i] = (byte) i;
        }
    }
    

    public void run() {
        try {
            menu.main_menu();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 65432);
            Client c = new Client(s);
            c.run();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}