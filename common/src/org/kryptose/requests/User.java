package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represents a user.
 * 
 * Contains sensitive information!
 * 
 * @author jshi
 */
public final class User implements Comparable<User>, Serializable {

	public static final String VALID_USERNAME_DOC =
			"Usernames must be 3-15 characters, consisting only of lowercase Latin letters a-z, Arabic digits 0-9, hyphen, and underscore.";
	public static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-z0-9_-]{3,15}$");  
	
    private final String username;
    private byte[] passkey;
    
    public static boolean isValidUsername(String username) {
    	return VALID_USERNAME_PATTERN.matcher(username).matches();
    }

    public User(String name, byte[] passkey) {
        this.username = name;
        if (passkey != null) this.passkey = passkey.clone();
        
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
    	if (!isValidUsername(this.username)) {
    		throw new IllegalArgumentException(VALID_USERNAME_DOC);
    	}
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
		return this.username.equals(other.username);
	}

}
