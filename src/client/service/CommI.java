package client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface CommI {
    public boolean authenticate(int tag, String uid, String password, DataInputStream is, DataOutputStream os) throws IOException;
    public void register(int tag, String uid, String password, DataInputStream is, DataOutputStream os) throws IOException;
    public void put(int tag, String key, byte[] data, DataInputStream is, DataOutputStream os) throws IOException;
    public byte[] get(int tag, String key, DataInputStream is, DataOutputStream os) throws IOException;
    public void multiPut(int tag, Map<String, byte[]> pairs, DataInputStream is, DataOutputStream os) throws IOException;
    public Map<String, byte[]> multiGet(int tag, Set<String> keys, DataInputStream is, DataOutputStream os) throws IOException;
    public void disconnect(int tag, DataInputStream is, DataOutputStream os) throws IOException;
}
