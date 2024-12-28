package server.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import server.service.*;
import shared.service.TaggedConnection;
import shared.service.TaggedConnection.KeyDataPair;
import shared.service.TaggedConnection.UidPassPair;
public class Server {
    private class ServerWorker implements Runnable {
        Manager man;
        AuthService authService;
        DataInputStream iStream;
        DataOutputStream oStream;

        public ServerWorker(Socket cs, Manager m, AuthService authService) throws IOException {
            this.man = m;
            this.authService = authService;
            iStream = new DataInputStream(cs.getInputStream());
            oStream = new DataOutputStream(cs.getOutputStream());
        }


        public void run() {
            //need to design protocol for this one

            //authenticate first, then do the rest (handle requests and allat)
            //flag to check if authenticated?
            boolean authenticated = false;
            Worker w = null;
            boolean has_quit = false;
            while (!has_quit) {
                try {
                    TaggedConnection t = TaggedConnection.deserialize(iStream);
                    int tag = t.get_tag();
                    Object obj = t.get_data();
                    String id = t.get_id();

                    if(!authenticated) {
                        System.out.println("Not auth!");
                        if(null == id) {
                            System.out.println("Unauthorized operation (" + id + ")");
                            TaggedConnection response = new TaggedConnection(tag, "Error", "no auth");
                            response.serialize(oStream);
                        } else switch (id) {
                            case "Login" ->                                 {
                                    UidPassPair upp = (UidPassPair) obj;
                                    System.out.println("Got Login message with uid " + upp.uid + " and pass " + upp.password);
                                    authenticated = authService.authenticate(upp.uid, upp.password);
                                    System.out.println("Auth status: " + authenticated);
                                    if(authenticated) {
                                        w = man.join();
                                    }       
                                    TaggedConnection response = new TaggedConnection(tag, "Data", authenticated);
                                    response.serialize(oStream);
                                }
                            case "Register" ->                                 {
                                    UidPassPair upp = (UidPassPair) obj;
                                    System.out.println("Got Register message with uid " + upp.uid + " and pass " + upp.password);
                                    authService.register_client(upp.uid, upp.password);
                                    TaggedConnection response = new TaggedConnection(tag, "Echo", "ok");
                                    response.serialize(oStream);
                                }
                            default ->                                 {
                                    System.out.println("Unauthorized operation (" + id + ")");
                                    TaggedConnection response = new TaggedConnection(tag, "Error", "no auth");
                                    response.serialize(oStream);
                                }
                        }
                    } else 
                    if(null != id) switch (id) {
                        case "Put" -> {
                            KeyDataPair kdp = (KeyDataPair) obj;
                            w.put(kdp.key, kdp.data);
                            TaggedConnection response = new TaggedConnection(tag, "Echo", "ok");
                            response.serialize(oStream);
                            }
                        case "Get" -> {
                            String key = (String) obj;
                            byte[] bArray = w.get(key);
                            TaggedConnection response = new TaggedConnection(tag, "Data", bArray); //not checking for null return 
                            response.serialize(oStream);
                            }
                        case "MultiPut" -> {
                            @SuppressWarnings("unchecked")
                                    Map<String, byte[]> m = (Map<String, byte[]>) obj;
                            w.multiPut(m);
                            TaggedConnection response = new TaggedConnection(tag, "Echo", "ok");
                            response.serialize(oStream);
                            }
                        case "MultiGet" -> {
                            @SuppressWarnings("unchecked")
                                    Set<String> s = (Set<String>) obj;
                            Map<String, byte[]> m = w.mutliGet(s);
                            TaggedConnection response = new TaggedConnection(tag, "Data", m); //not checking for null return 
                            response.serialize(oStream);
                            }
                        case "Disconnect" -> {
                            w.leave();
                            TaggedConnection response = new TaggedConnection(tag, "Data", true); //not checking for null return 
                            response.serialize(oStream);
                            has_quit = true;
                            }
                        default -> {
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.toString());
                    break;
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
