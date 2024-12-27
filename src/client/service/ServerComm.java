package client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.Set;
import server.service.TaggedConnection;

public class ServerComm implements CommI {
    private final Multiplexer multiplexer;

    public ServerComm(DataInputStream is, DataOutputStream os) {
        multiplexer = new Multiplexer(is, os);
    }

    @Override
    public boolean authenticate(int tag, String uid, String password) throws Exception {
        //send login message, wait for response, check if response has same tag(?), check response value, return response
        TaggedConnection send = new TaggedConnection(tag, "Login", new TaggedConnection.UidPassPair(uid, password));
        multiplexer.enqueue(send);
        TaggedConnection response = multiplexer.dequeue(tag);
        //return value (bool)
        if("Error".equals(response.get_id())) return false;
        return (boolean) response.get_data();
    }

    @Override
    public boolean register(int tag, String uid, String password) throws Exception {
        TaggedConnection send = new TaggedConnection(tag, "Register", new TaggedConnection.UidPassPair(uid, password));
        multiplexer.enqueue(send);
        TaggedConnection response = multiplexer.dequeue(tag);
        return !"Error".equals(response.get_id());
    }

    @Override
    public boolean put(int tag, String key, byte[] data) throws Exception {
        TaggedConnection send = new TaggedConnection(tag, "Put", new TaggedConnection.KeyDataPair(key, data));
        multiplexer.enqueue(send);
        TaggedConnection response = multiplexer.dequeue(tag);
        return !"Error".equals(response.get_id());
    }

    @Override
    public byte[] get(int tag, String key) throws Exception {
        TaggedConnection send = new TaggedConnection(tag, "Get", key);
        multiplexer.enqueue(send);
        TaggedConnection response = multiplexer.dequeue(tag);
        if("Error".equals(response.get_id())) return null;
        return (byte[]) response.get_data();
    }

    @Override
    public boolean multiPut(int tag, Map<String, byte[]> pairs) throws Exception {
        TaggedConnection send = new TaggedConnection(tag, "MultiPut", pairs);
        multiplexer.enqueue(send);
        TaggedConnection response = multiplexer.dequeue(tag);
        return !"Error".equals(response.get_id());
    }

    @Override
    public Map<String, byte[]> multiGet(int tag, Set<String> keys) throws Exception {
        TaggedConnection send = new TaggedConnection(tag, "MultiGet", keys);
        multiplexer.enqueue(send);
        TaggedConnection response = multiplexer.dequeue(tag);
        if("Error".equals(response.get_id())) return null;

        @SuppressWarnings("unchecked")
        Map<String, byte[]> ret = (Map<String, byte[]>) response.get_data();
        return ret;
    }
    
    @Override
    public boolean disconnect(int tag) throws Exception {
        TaggedConnection send = new TaggedConnection(tag, "Disconnect", "ok");
        multiplexer.enqueue(send);
        TaggedConnection response = multiplexer.dequeue(tag);
        return !"Error".equals(response.get_id());
    }

    public void runningSend() throws Exception {
        multiplexer.runningSend();
    }

    public void runningReceive() throws Exception {
        multiplexer.runningReceive();
    }


}
