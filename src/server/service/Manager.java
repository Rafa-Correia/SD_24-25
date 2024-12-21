package server.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.concurrent.locks.Condition;

public class Manager {
    private int MAX_COUNT;

    private DataService dataService;

    private int client_count;
    private Lock l;
    private Condition max_clients_reached;

    public Manager(int max) {
        MAX_COUNT = max;
        client_count = 0;
        l = new ReentrantLock();
        max_clients_reached = l.newCondition();

        dataService = DataService.getInstance();
    }
    
    
    public Worker join () throws InterruptedException {
        l.lock();
        try {
            while(client_count >= MAX_COUNT) {
                max_clients_reached.await();
            }
            client_count++;

            Worker w = new Worker(dataService, this);
            return w;
            
        } finally {
            l.unlock();
        }
    } 

    public void leave() {
        l.lock();
        try {
            client_count--;
            max_clients_reached.signalAll();
        } finally {
            l.unlock();
        }
    }


}