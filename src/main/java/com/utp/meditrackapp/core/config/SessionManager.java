package com.utp.meditrackapp.core.config;

import com.utp.meditrackapp.core.models.entity.Usuario;
public class SessionManager {
    private static SessionManager instance;
    private Usuario currentUser;

    private SessionManager() {
       
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Usuario user){
        this.currentUser = user;
    }

    public void logout(){
        this.currentUser = null;
    }

     public Usuario getCurrentUser() {
        return currentUser;
     }

     public boolean isLoggedIn() {
        return currentUser != null;
     }
}
