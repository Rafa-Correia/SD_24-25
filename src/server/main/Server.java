package server.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

import server.service.*;
import server.service.TaggedConnection.KeyDataPair;
import server.service.TaggedConnection.UidPassPair;
public class Server {
    private class ServerWorker implements Runnable {
        Manager m;
        AuthService authService;
        DataInputStream iStream;
        DataOutputStream oStream;

        public ServerWorker(Socket cs, Manager m, AuthService authService) throws IOException {
            this.m = m;
            this.authService = authService;
            iStream = new DataInputStream(cs.getInputStream());
            oStream = new DataOutputStream(cs.getOutputStream());
        }


        public void run() {
            //need to design protocol for this one

            //authenticate first, then do the rest (handle requests and allat)
            //flag to check if authenticated?
            boolean authenticated = false;
            while (true) {
                try {
                    TaggedConnection t = TaggedConnection.deserialize(iStream);
                    int tag = t.get_tag();
                    Object obj = t.get_data();
                    String id = t.get_id();
                    Worker w = null;
                    if(!authenticated) {
                        if("Login".equals(id)) {
                            UidPassPair upp = (UidPassPair) obj;
                            authenticated = authService.authenticate(upp.uid, upp.password);
                            if(authenticated) {
                                w = m.join();
                            }
                            TaggedConnection response = new TaggedConnection(tag, "Data", authenticated);
                            response.serialize(oStream);
                        } if("Register".equals(id)) {
                            UidPassPair upp = (UidPassPair) obj;
                            authService.register_client(upp.uid, upp.password);
                            TaggedConnection response = new TaggedConnection(tag, "Echo", "ok");
                            response.serialize(oStream);
                        } else {
                            continue;
                        }
                    }
                    if("Put".equals(id)) {
                        KeyDataPair kdp = (KeyDataPair) obj;
                        w.put(kdp.key, kdp.data);
                        TaggedConnection response = new TaggedConnection(tag, "Echo", "ok");
                        response.serialize(oStream);
                    } else if("Get".equals(id)) {
                        String key = (String) obj;
                        byte[] bArray = w.get(key);
                        TaggedConnection response = new TaggedConnection(tag, "Data", bArray); //not checking for null return 
                        response.serialize(oStream);
                    } else if("MultiPut".equals(id)) {
                        @SuppressWarnings("unchecked")
                        Map<String, byte[]> m = (Map<String, byte[]>) obj;
                        w.multiPut(m);
                        TaggedConnection response = new TaggedConnection(tag, "Echo", "ok");
                        response.serialize(oStream);
                    } else if("MultiGet".equals(id)) {
                        @SuppressWarnings("unchecked")
                        Set<String> s = (Set<String>) obj;
                        Map<String, byte[]> m = w.mutliGet(s);
                        TaggedConnection response = new TaggedConnection(tag, "Data", m); //not checking for null return 
                        response.serialize(oStream);
                    } else if("Disconnect".equals(id)) {
                        w.leave();
                        TaggedConnection response = new TaggedConnection(tag, "Echo", "ok"); //not checking for null return 
                        response.serialize(oStream);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                
            }
            

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
