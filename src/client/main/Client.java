package client.main;

import client.service.CommI;
import client.service.ServerComm;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Client {
    private CommI coms;
    private DataInputStream is;
    private DataOutputStream os;
    private int tag_counter = 0;

    private byte[] msg = new byte[10];
    


    public Client (Socket s) throws IOException {
        this.coms = new ServerComm();
        this.is = new DataInputStream(s.getInputStream());
        this.os = new DataOutputStream(s.getOutputStream());

        for (int i = 0; i < msg.length; i++) {
            msg[i] = (byte) i;
        }
    }
    

    public void run() throws Exception {
        coms.register(tag_counter, "admin", "admin", is, os);
        tag_counter++;
        coms.authenticate(tag_counter, "admin", "admin", is, os);
        tag_counter++;


        while (true) { 
            System.out.println("On tag " + tag_counter + " with key " + tag_counter + " putting " + Arrays.toString(msg));
            coms.put(tag_counter, String.valueOf(tag_counter), msg, is, os);
            tag_counter++;
            System.out.println("On tag " + tag_counter + " with key " + (tag_counter-1) + " getting data.");
            byte[] put_data = coms.get(tag_counter, String.valueOf(tag_counter-1), is, os);
            tag_counter++;
            System.out.println(Arrays.toString(put_data));
            TimeUnit.SECONDS.sleep(5);
        }
    }
    

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 65432);
            Client c = new Client(s);
            c.run();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}