package com.utp.meditrackapp.core.config;

import com.utp.meditrackapp.domain.entities.Usuario;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();
    private volatile Usuario currentUser;

    private SessionManager() {
       
    }
    
    public static SessionManager getInstance() {
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

     public boolean isTecnico() {
         return currentUser != null && currentUser.isTecnico();
     }

     public boolean isQuimico() {
         return isLoggedIn() && "ROL-00-0000002".equals(currentUser.getRolId());
     }

     public boolean isAdmin() {
         return currentUser != null && currentUser.isAdmin();
     }
}
