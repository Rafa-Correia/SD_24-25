package client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import server.service.TaggedConnection;

public class Multiplexer {
    private DataInputStream is;
    private DataOutputStream os;

    private List<TaggedConnection> sendQueue;
    private Map<Integer, MEntry> receiveQueue;

    private Lock l = new ReentrantLock();

    private Condition sendQueue_empty = l.newCondition();

    public Multiplexer(DataInputStream is, DataOutputStream os) {
        this.sendQueue = new ArrayList<>();
        this.receiveQueue = new HashMap<>();

        this.is = is;
        this.os = os;
    }

    public void enqueue (TaggedConnection tc) {
        l.lock();
        try {
            Condition c = l.newCondition();
            MEntry new_entry = new MEntry(c);
            receiveQueue.put(tc.get_tag(), new_entry);

            sendQueue.add(tc);
            sendQueue_empty.signalAll();
        } finally {
            l.unlock();
        }
    }

    public TaggedConnection dequeue (int tag) {
        //await until message with given tag is received 
        //when wakes up remove from map and return taggedConnection

        return null;
    }

    private static class MEntry {
        boolean is_ready_flag;
        Condition c;
        TaggedConnection tc;

        MEntry(Condition c) {
            is_ready_flag = false;
            this.c = c;
            tc = null;
        }

        void setEntryData(TaggedConnection tc) {
            is_ready_flag = true;
            this.tc = tc;
            c.signalAll();
        }
    }

    public void runningSend () { //infinitelly checks if send queue is empty and sends objects on queue

    } 

    public void runningReceive() { //keeps checking for incoming messages, places them in the receiveQueue (signaling waiting threads)

    }
}
