package server.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import server.model.Data;

/*
 * DataService is a singleton aimed at providing a service capable of managing data storage and retrieval by others.
 */

public class DataService {
    private static DataService instance = null;
    private Data data_inst;

    private Lock l;
    
    /*
     *  Private constructor for singleton.
     */
    private DataService() {
        data_inst = Data.getInstance();
        l = new ReentrantLock();
        instance = this;
    }

    /*
     *  Public retriever of singleton instance.
     */
    public static DataService getInstance() {
        if (instance == null) {
            new DataService();
        }

        return instance;
    }



    public void put(String key, byte[] data) {
        l.lock();
        try {
            data_inst.put_data(key, data);
        } finally {
            l.unlock();
        }
    }

    public byte[] get(String key) {
        l.lock();
        try {
            return data_inst.get_data(key);
        } finally {
            l.unlock();
        }
    }
}
