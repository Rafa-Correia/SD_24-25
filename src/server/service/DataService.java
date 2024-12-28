package server.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import server.model.Data;

/**
 * DataService is a singleton aimed at providing a service capable of managing data storage and retrieval by others.
 */

class DataService {
    private static DataService instance = null;
    private Data data_inst;

    private Lock l;
    
    /**
     *  Private constructor for singleton.
     */
    private DataService() {
        data_inst = Data.getInstance();
        l = new ReentrantLock();
        instance = this;
    }

    /**
     *  Public retriever of singleton instance.
     */
    public static DataService getInstance() {
        if (instance == null) {
            new DataService();
        }

        return instance;
    }


    /**
     * Puts data in storage (memory) using an identifier.
     * 
     * 
     * @param key
     * @param data
     */
    public void put(String key, byte[] data) {
        l.lock();
        try {
            data_inst.put_data(key, data);
        } finally {
            l.unlock();
        }
    }

    /**
     * 
     * @param key
     * @return data associated with given key. Returns null if no data associated with key.
     */
    public byte[] get(String key) {
        l.lock();
        try {
            return data_inst.get_data(key);
        } finally {
            l.unlock();
        }
    }

    /**
     * Puts multiple blocks of data in storage, each block with it's own unique identifier.
     * 
     * @param pairs
     */
    public void multiPut(Map<String, byte[]> pairs) {
        Set<String> keySet = pairs.keySet();
        l.lock();
        try {
            for(String key : keySet) {
                data_inst.put_data(key, pairs.get(key));
            }
        } finally {
            l.unlock();
        }
    }


    /**
     * 
     * 
     * @param keys
     * @return Map of key data pairs. Each data block is correspondant to it's key in the given keyset.
     */
    public Map<String, byte[]> multiGet(Set<String> keys) {
        Map<String, byte[]> ret = new HashMap<>();
        l.lock();
        try {
            for(String key : keys) {
                ret.put(key, data_inst.get_data(key));
            }
            return ret;
        } finally {
            l.unlock();
        }
    }
}
