package client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class Menu {
    private final ServerComm sCom;
    private final Scanner sc = new Scanner(System.in);

    private int tag_counter = 0;

    private boolean is_multithreaded = false;

    //still no idea what instance variables are needed
    //todo
    public Menu (DataInputStream is, DataOutputStream os) {
        sCom = new ServerComm(is, os); //all that's needed maybe?
        startup();
    }

    private void clearConsole() {
        for (int i = 0; i < 10; i++) {
            System.out.println("\n");
        }
    }

    private static class WriterReader implements Runnable {
        private final ServerComm m;
        private boolean is_writer = true;

        public WriterReader (ServerComm m, boolean is_writer) {
            this.m = m;
            this.is_writer = is_writer;
        }

        @Override
        public void run () {
            try {
                if(is_writer) {
                    m.runningSend();
                }
                else m.runningReceive();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    /**
     * Creates two threads, one to run the writing cycle of the Multiplexer and another
     * to run the reading cycle. Both are started here. 
    */
    private void startup() {
        WriterReader writer = new WriterReader(sCom, true);
        WriterReader reader = new WriterReader(sCom, false);

        Thread tW = new Thread(writer);
        Thread tR = new Thread(reader);

        tW.start();
        tR.start();
    }

    public void startup_menu() throws Exception {
        System.out.println("There's two ways to run the client, single and multithreaded.\nIn \"singlethreaded mode\" the client will handle a single request at once.\nIn \"multithreaded mode\" the client will be able to run multiple requests at once.\nIf the server is being slow it is recomended to use \"multithreaded mode\".\nSo, which will it be?\n");
        System.out.println("1 - Singlethreaded\n2 - Multithreaded ");

        while(true) {
            System.out.print("\nMode: ");
            int mode = sc.nextInt();

            if(mode != 1 && mode != 2) System.out.println("Not a valid option.\n");
            else {
                is_multithreaded = mode != 1;
                break;
            }
        }
        main_menu();
    }

    private void main_menu() throws Exception {
        boolean has_quit = false;

        while (has_quit == false) { 
            if(has_quit == true) break;
            //show options
            System.out.println("=========================================================\n");
            System.out.println("1 - Register ");
            System.out.println("2 - Login ");
            System.out.println("3 - Upload ");
            System.out.println("4 - Download ");
            System.out.println("5 - Upload multiple files ");
            System.out.println("6 - Download multiple files ");
            System.out.println("0 - Disconnect and leave ");
            //read selected option
            System.out.print("\n\nPlease enter a selection: ");

            int selection = sc.nextInt();

            clearConsole();

            if(selection < 0 || selection > 6) {
                System.out.println("Not supported, try again!");
            }
            else {
                switch(selection) {
                    case 1 -> register_menu();
                    case 2 -> login_menu();
                    case 3 -> upload_single_menu();
                    case 4 -> download_single_menu();
                    case 5 -> upload_many_menu();
                    case 6 -> download_many_menu();
                    case 0 -> {
                        has_quit = disconnect();
                        System.out.println("(main) " + has_quit);
                        break;
                    }
                    default -> {
                    }
                }
            }
        }
    }


    private void register_menu() throws Exception {
        try {
            System.out.println("=============================================\n");
            System.out.print("User ID: ");
            String uid = sc.next();
            System.out.print("Password: ");
            String password = sc.next();

            boolean authenticated = sCom.register(tag_counter, uid, password);
            tag_counter++;

            clearConsole();

            if(authenticated) {
                System.out.println("Registration Successfull!");
            } else {
                System.out.println("Couldn't register!");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void login_menu() throws Exception {
        try {
            System.out.println("=============================================\n");
            System.out.print("User ID: ");
            String uid = sc.next();
            System.out.print("Password: ");
            String password = sc.next();

            boolean authenticated = sCom.authenticate(tag_counter, uid, password);
            tag_counter++;

            clearConsole();

            if(authenticated) {
                System.out.println("Login Successfull!");
            } else {
                System.out.println("Couldn't login!");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void upload_single_menu() {
        try {
            System.out.print("Please enter the file path: ");
            String filePath = sc.next();
            System.out.print("What key would you like to associate with the file?\nKey: ");
            String key = sc.next();

            Path path = Path.of(filePath);
            byte[] data = Files.readAllBytes(path);

            boolean status = sCom.put(tag_counter, key, data);
            tag_counter++;

            clearConsole();

            if(status) {
                System.out.println("Success!");

            } else {
                System.out.println("Could not upload file " + filePath + " with key " + key + ".");
            }
        } catch (Exception e) {
            System.out.println("File does not exist!");
        }
    }

    private void download_single_menu() {
        try {
            System.out.print("What's the key of the file?\nKey: ");
            String key = sc.next();
            Path p = Path.of(key);

            byte[] file_dl = sCom.get(tag_counter, key);
            tag_counter++;

            clearConsole();

            if(file_dl == null) System.out.println("Failed to download.");
            else {
                Files.write(p, file_dl);
                System.out.println("Success!");
            }
        } catch (Exception e) {
            System.out.println("Failed to download (" + e.toString() + ").");
        }
    }

    private void upload_many_menu() {
        //hmm
        try {
            int counter = 1;
            Map<String, byte[]> pairs = new HashMap<>();
            System.out.println("Please state the file path of the files you intend to upload and the keys to which you intend to associate them with.\nWhen you want to stop, enter \"exit\"");
            while (true) { 
                System.out.print("File path ("+counter+"): ");
                String path = sc.next();
                if("exit".equals(path)) break;
                System.out.print("Key ("+counter+"): ");
                String key = sc.next();

                Path p = Path.of(path);
                boolean exists = Files.exists(p);
                if(exists) {
                    byte[] file_data = Files.readAllBytes(p);
                    pairs.put(key, file_data);
                    System.out.println("Ok.");
                } else {
                    System.out.println("File does not exist.");
                }
                System.out.print("\n\n");

                counter++;
            }

            clearConsole();

            if(!pairs.isEmpty()) {
                boolean status = sCom.multiPut(tag_counter, pairs);
                tag_counter++;

                if(status) {
                    System.out.println("Success!");
                } else {
                    System.out.println("Something went wrong.");
                }
            } else {
                System.out.println("No files to be uploaded.");
            }

        } catch (Exception e) {
        }
    }

    private void download_many_menu() {
        try {
            //hmm2
            System.out.println("Please state the keys of the files you intend to download.\nWhen you want no more keys, enter \"exit\".");
            Set<String> keys = new HashSet<>();
            int counter = 1;
            while(true) {
                System.out.print("Key " + counter + ": ");
                String key = sc.next();
                if("exit".equals(key)) break;
                keys.add(key);
                counter++;
            }

            Map<String, byte[]> file_data = sCom.multiGet(tag_counter, keys);
            tag_counter++;

            for (Entry<String, byte[]> e : file_data.entrySet()) {
                Path p = Path.of(e.getKey());
                Files.write(p, e.getValue());
            }

            clearConsole();

            System.out.println("Success!");
        } catch (Exception e) {

        }
    }

    private boolean disconnect() {
        try {
            boolean success = sCom.disconnect(tag_counter);
            tag_counter++;
            System.out.println("Has disconected: " + success);
            return success;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }
}
