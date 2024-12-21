package server.service;

import server.model.Auth;


/*
 * AuthService is a singleton aimed at providing a service capable of user authentication 
 * by means of storing a password associated with a user identifier (uid).
 */
public class AuthService {
    private static AuthService instance = null;
    private Auth auth;
    

    private AuthService() {
        auth = Auth.getInstance();
        instance = this;
    }

    public static AuthService getInstance() {
        if (instance == null) {
            new AuthService();
        }

        return instance;
    }

    public void register_client(String uid, String password) {
        auth.put_details(uid, password);
    }

    public boolean authenticate(String uid, String password) {
        String uid_pass = auth.get_password(uid);
        if(uid_pass == null) return false; //check if null
        return uid_pass.equals(password);
    }
}
