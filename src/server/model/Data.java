package server.model;

import java.util.HashMap;
import java.util.Map;

public class Data {
    private static Data instance = null;
    private Map<String, byte[]> dataMap;

    private Data() {
        dataMap = new HashMap<>();
        instance = this;
    }

    public static Data getInstance() {
        if(instance == null) {
            new Data();
        }

        return instance;
    }


    public void put_data(String key, byte[] data) {
        dataMap.put(key, data);
    }

    public byte[] get_data(String key) {
        if(dataMap.containsKey(key)) {
            return dataMap.get(key);
        }
        return null;
    }
}
