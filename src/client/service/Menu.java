package client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Menu {
    private final ServerComm sCom;
    private final Scanner sc = new Scanner(System.in);

    private int tag_counter = 0;

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

    public void main_menu() throws Exception {
        boolean has_quit = false;

        while (!has_quit) { 
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
                    case 0 -> has_quit = disconnect();
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
             
            main_menu();
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
             
            main_menu();
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
