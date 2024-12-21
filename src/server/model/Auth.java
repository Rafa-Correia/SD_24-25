package server.model;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    private static Auth instance = null;

    private Map<String, String> authMap;    //user id -> password

    
    private Auth() {
        authMap = new HashMap<>();
        instance = this;
    }

    public static Auth getInstance() {
        if (instance == null) {
            new Auth();
        }
        return instance;
    }

    public void put_details(String uid, String password) {
        authMap.put(uid, password);
    }

    public String get_password(String uid) {
        if(authMap.containsKey(uid)) {
            return authMap.get(uid);
        }
        return null;
    }

}
