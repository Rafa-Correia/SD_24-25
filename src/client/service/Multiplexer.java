package client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import server.service.TaggedConnection;

public class Multiplexer {
    private final DataInputStream is;
    private final DataOutputStream os;

    private final List<TaggedConnection> sendQueue;
    private final Map<Integer, MEntry> receiveQueue;

    private final Lock l = new ReentrantLock();

    private final Condition sendQueue_empty = l.newCondition();

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

    /**
     * This function will stop the calling thread until the response with the desired tag is received.
     * 
     * @param tag
     * @return Returns TaggedConnection with given tag.
     * @throws InterruptedException
     */
    public TaggedConnection dequeue (int tag) throws InterruptedException {
        l.lock(); 
        try {
            MEntry e = receiveQueue.get(tag);
            while(!e.is_ready_flag) {
                e.c.await();
            }
            receiveQueue.remove(tag);
            return e.tc;
        } finally {
            l.unlock();
        }
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

    public void runningSend () throws Exception { //infinitelly checks if send queue is empty and sends objects on queue
        l.lock();
        try {
            while (true) { 
                while(sendQueue.isEmpty()) {
                    sendQueue_empty.await(); //releases lock when queue is empty!
                }
                TaggedConnection send = sendQueue.remove(0);
                send.serialize(os);
            }
        } finally {
            l.unlock();
        }
    } 

    public void runningReceive() throws IOException { //keeps checking for incoming messages, places them in the receiveQueue (signaling waiting threads)
        while(true) {
            TaggedConnection tc = TaggedConnection.deserialize(is);
            int tag = tc.get_tag();
            l.lock();
            try {
                MEntry e = receiveQueue.get(tag);
                e.setEntryData(tc);
                e.c.signalAll();
            } finally {
                l.unlock();
            }
        }
    }  
}  
