package client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import server.service.TaggedConnection;

public class ServerComm implements CommI {
    @Override
    public boolean authenticate(int tag, String uid, String password, DataInputStream is, DataOutputStream os) throws IOException {
        //send login message, wait for response, check if response has same tag(?), check response value, return response
        TaggedConnection send = new TaggedConnection(tag, "Login", new TaggedConnection.UidPassPair(uid, password));
        send.serialize(os);
        TaggedConnection response = TaggedConnection.deserialize(is);
        //return value (bool)
        return (boolean) response.get_data();
    }

    @Override
    public void register(int tag, String uid, String password, DataInputStream is, DataOutputStream os) throws IOException {
        TaggedConnection send = new TaggedConnection(tag, "Register", new TaggedConnection.UidPassPair(uid, password));
        send.serialize(os);
        TaggedConnection.deserialize(is); //clear data in stream
    }

    @Override
    public void put(int tag, String key, byte[] data, DataInputStream is, DataOutputStream os) throws IOException {
        TaggedConnection send = new TaggedConnection(tag, "Put", new TaggedConnection.KeyDataPair(key, data));
        send.serialize(os);
        TaggedConnection.deserialize(is); //clear data in stream
    }

    @Override
    public byte[] get(int tag, String key, DataInputStream is, DataOutputStream os) throws IOException {
        TaggedConnection send = new TaggedConnection(tag, "Get", key);
        send.serialize(os);
        TaggedConnection response = TaggedConnection.deserialize(is);
        return (byte[]) response.get_data();
    }

    @Override
    public void multiPut(int tag, Map<String, byte[]> pairs, DataInputStream is, DataOutputStream os) throws IOException {
        TaggedConnection send = new TaggedConnection(tag, "MultiPut", pairs);
        send.serialize(os);
        TaggedConnection.deserialize(is); //clear input stream
    }

    @Override
    public Map<String, byte[]> multiGet(int tag, Set<String> keys, DataInputStream is, DataOutputStream os) throws IOException {
        TaggedConnection send = new TaggedConnection(tag, "MultiGet", keys);
        send.serialize(os);
        TaggedConnection response = TaggedConnection.deserialize(is);
        @SuppressWarnings("unchecked")
        Map<String, byte[]> ret = (Map<String, byte[]>) response.get_data();
        return ret;
    }
    
    @Override
    public void disconnect(int tag, DataInputStream is, DataOutputStream os) throws IOException {
        TaggedConnection send = new TaggedConnection(tag, "Disconnect", "ok");
        send.serialize(os);
        TaggedConnection.deserialize(is); //clear input stream
    }
}
