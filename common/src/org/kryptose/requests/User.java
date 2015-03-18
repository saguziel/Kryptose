package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Represents a user.
 * 
 * Contains sensitive information!
 * 
 * @author jshi
 */
public final class User implements Comparable<User>, Serializable {

    private final String username;
    private byte[] passkey;

    public User(String name) {
        this.username = name;
        this.passkey = new byte[48].clone(); // TODO set passkey
        this.validateInstance();
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPasskey() {
    	// TODO think about security implications of public getPasskey
    	// Ideally the passkey would be erased from memory as soon as the user
    	// is authenticated, but that's infeasible to control precisely given
    	// Java's memory model anyway.
        return passkey.clone();
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Check that our invariants are satisfied
        this.validateInstance();
    }
    
    void validateInstance() {
    	if (this.username == null) throw new IllegalArgumentException("username is null");
    	if (this.passkey != null) this.passkey = passkey.clone();
    	//if (this.passkey == null) throw new IllegalArgumentException("passkey is null");
    }

    @Override
    public int compareTo(User u){
        return this.username.compareTo(u.getUsername());
    }

	@Override
	public int hashCode() {
		return username.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
