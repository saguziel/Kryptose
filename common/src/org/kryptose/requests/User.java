package org.kryptose.requests;

import java.io.Serializable;

/**
 * Represents a user.
 * 
 * @author jshi
 */
public class User implements Comparable<User>, Serializable {

    String username;
    String password;

    public User(String name) {
        this.username = name;
        this.password = "mpass";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int compareTo(User u){
        return this.username.compareTo(u.getUsername());
    }

}
