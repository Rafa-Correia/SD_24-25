package server.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TaggedConnection {
    private final int tag;
    private final int type; //
    private final Object data;

    public TaggedConnection (int tag, Object data) {
        this.tag = tag;
        this.data = data;
        this.type = inferType(data);

    }

    private int inferType(Object data) { //if need more just add more returns here
        if(data instanceof Map) {
            return 1;
        } else if(data instanceof Set) {
            return 2;
        } else if(data instanceof String) {
            return 3;
        } else if(data instanceof byte[]) {
            return 4;
        } else if(data instanceof UidPassPair) {
            return 5;
        } else if(data instanceof KeyDataPair) {
            return 6;
        } else if(data instanceof Boolean) {
            return 7;
        } else throw new IllegalArgumentException("Unsupported data type: " + data.getClass());
    }

    public static class UidPassPair {
        public String uid;
        public String password;

        public UidPassPair(String uid, String password) {
            this.uid = uid;
            this.password = password;

        }
    }

    public static class KeyDataPair{
        public String key;
        public byte[] data;

        public KeyDataPair(String key, byte[] data) {
            this.key = key;
            this.data = data;
        }
    }


    public void serialize(DataOutputStream os) throws IOException {
        os.writeInt(tag);
        os.writeInt(type);

        switch(type) {
            case 1:
                @SuppressWarnings("unchecked")
                Map<String, byte[]> m = (Map<String, byte[]>) data;
                os.writeInt(m.size());
                for(Map.Entry<String, byte[]> e : m.entrySet()) {
                    os.writeUTF(e.getKey());
                    byte[] v = e.getValue();
                    os.writeInt(v.length);
                    os.write(v);
                }
                os.flush();
                break; 
            
            case 2:
                @SuppressWarnings("unchecked")
                Set<String> s = (Set<String>) data;
                os.writeInt(s.size());
                for(String key : s) {
                    os.writeUTF(key);
                }
                os.flush();
                break;

            case 3:
                //string
                os.writeUTF((String) data);
                os.flush();
                break;

            case 4:
                //byte[]
                byte[] bArray = (byte[]) data;
                os.writeInt(bArray.length);
                os.write(bArray);
                os.flush();
                break;

            case 5:
                //UidPassPair
                UidPassPair p = (UidPassPair) data;
                os.writeUTF(p.uid);
                os.writeUTF(p.password);
                os.flush();
                break;
            
            case 6:
                //KeyDataPair
                KeyDataPair k = (KeyDataPair) data;
                os.writeUTF(k.key);
                os.writeInt(k.data.length);
                os.write(k.data);
                os.flush();
                break;

            case 7:
                //boolean
                Boolean b = (Boolean) data;
                os.writeBoolean(b);
                os.flush();
                break;

            default:
                break;
        }
    }

    public static TaggedConnection deserialize(DataInputStream is) throws IOException {
        int tag = is.readInt();
        int type = is.readInt();
        Object data;
        switch(type) {
            case 1:
                //map
                Map<String, byte[]> m = new HashMap<>();
                int mapsize = is.readInt();
                for (int i = 0; i < mapsize; i++) {
                    String key = is.readUTF();
                    int arraysize = is.readInt();
                    byte[] arr = new byte[arraysize];
                    is.readFully(arr);
                    m.put(key, arr);
                }
                data = m;
                break;

            case 2:
                //set
                Set<String> s = new HashSet<>();
                int setsize = is.readInt();
                for (int i = 0; i < setsize; i++) {
                    s.add(is.readUTF());
                }
                data = s;
                break;

            case 3:
                //string
                data = is.readUTF();
                break;
            
            case 4:
                int arraysize = is.readInt();
                byte[] bytearray = new byte[arraysize];
                is.readFully(bytearray);
                data = bytearray;
                break;

            case 5:
                String uid = is.readUTF();
                String password = is.readUTF();

                data = new UidPassPair(uid, password);
                break;

            case 6:
                String key = is.readUTF();
                int bSize = is.readInt();
                byte[] bArray = new byte[bSize];
                data = new KeyDataPair(key, bArray);
                break;

            case 7:
                data = is.readBoolean();
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported type.");

        }

        return new TaggedConnection(tag, data);
    }



    public Object get_object() {
        return data;
    }
}
