package server.service;

import java.util.Map;
import java.util.Set;

//will handle requests and all that
public class Worker {
    private DataService dService;
    private Manager manager; //keep as reference to call leave();

    public Worker(DataService ds, Manager m) {
        this.dService = ds;
        this.manager = m;
    }

    public void put(String key, byte[] data) {
        dService.put(key, data);
    }


    public byte[] get(String key) {
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
