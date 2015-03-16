package org.kryptose.requests;

import java.io.Serializable;

/**
 * Represents a user.
 * 
 * @author jshi
 */
public class User implements Comparable<User>, Serializable {

    String username;

    public User(String name) {
        this.username = name;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int compareTo(User u){
        return this.username.compareTo(u.getUsername());
    }

}
