package client.service;

import java.util.Map;
import java.util.Set;

public interface CommI {
    public boolean authenticate(int tag, String uid, String password) throws Exception;
    public boolean register(int tag, String uid, String password) throws Exception;
    public boolean put(int tag, String key, byte[] data) throws Exception;
    public byte[] get(int tag, String key) throws Exception;
    public boolean multiPut(int tag, Map<String, byte[]> pairs) throws Exception;
    public Map<String, byte[]> multiGet(int tag, Set<String> keys) throws Exception;
    public boolean disconnect(int tag) throws Exception;
}
