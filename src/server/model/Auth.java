package server.model;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    private static final Auth INSTANCE = new Auth();

    private final Map<String, String> authMap;    //user id -> password

    
    private Auth() {
        authMap = new HashMap<>();
    }

    public static Auth getInstance() {
        return INSTANCE;
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
