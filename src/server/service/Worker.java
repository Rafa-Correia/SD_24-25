package server.service;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

//will handle requests and all that
public class Worker {
    private final DataService dService;
    private final Manager manager; //keep as reference to call leave();

    public Worker(DataService ds, Manager m) {
        this.dService = ds;
        this.manager = m;
    }

    public void put(String key, byte[] data) {
        System.out.println("Trying to put on key " + key + " data " + Arrays.toString(data));
        dService.put(key, data);
        System.out.println("Done!");
    }


    public byte[] get(String key) {
        System.out.println("Trying to get from key " + key);
        return dService.get(key);
    }


    public void multiPut(Map<String, byte[]> pairs) {
        dService.multiPut(pairs);
    }

    public Map<String, byte[]> mutliGet(Set<String> keys) {
        return dService.multiGet(keys);
    }


    public void leave() {
        manager.leave();
    }
}
