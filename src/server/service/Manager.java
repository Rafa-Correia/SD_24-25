package server.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import server.model.Auth;
import server.model.Data;

import java.util.concurrent.locks.Condition;

public class Manager {
    private static final int MAX_COUNT = 10;

    private static Manager instance = null; //singleton instance

    private AuthService authService;
    private DataService dataService;

    private int client_count;
    private Lock l;
    private Condition max_clients_reached;

    private Manager() {
        client_count = 0;
        l = new ReentrantLock();
        max_clients_reached = l.newCondition();

        
        authService = AuthService.getInstance();
        dataService = DataService.getInstance();

        instance = this;
    }

    public static Manager getInstance() {
        if(instance == null) new Manager();
        return instance;
    }

    public void handle_request() {
        //something something
    }
}
