package server.service;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Manager {
    private final int MAX_COUNT;

    private final DataService dataService;

    private int client_count;
    private final Lock l;
    private final Condition max_clients_reached;

    public Manager(int max) {
        MAX_COUNT = max;
        client_count = 0;
        l = new ReentrantLock();
        max_clients_reached = l.newCondition();

        dataService = DataService.getInstance();
    }
    
    
    public Worker join() throws InterruptedException {
        //System.out.println("Waiting on join...");
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
            //System.out.println("Done!");
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