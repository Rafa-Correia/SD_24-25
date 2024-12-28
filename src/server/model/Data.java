package server.model;

import java.util.HashMap;
import java.util.Map;

public class Data {
    private static final Data INSTANCE = new Data();
    private final Map<String, byte[]> dataMap;

    private Data() {
        dataMap = new HashMap<>();
    }

    public static Data getInstance() {
        return INSTANCE;
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
