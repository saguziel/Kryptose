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
 * Also is immutable.
 * 
 * @author jshi
 */
public final class User implements Comparable<User>, Serializable {

	//TODO: perhaps too restrictive. I cannot use an email address or even an uppercase letter.
	public static final String VALID_USERNAME_DOC =
			"Usernames must be 3-15 characters, consisting only of lowercase Latin letters a-z, Arabic digits 0-9, hyphen, and underscore.";
	public static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-z0-9_-]{3,15}$");  
	
    private final String username;
    private byte[] passkey;
    
    /**
     * Checks a username for validity.
     * 
     * @param username The candidate username whose validity to check.
     * @return true if and only if the username is valid.
     * 
     * @see #VALID_USERNAME_DOC
     * @see #VALID_USERNAME_PATTERN
     */
    public static boolean isValidUsername(String username) {
    	return VALID_USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Constructs a User.
     * 
     * @param name The username of the user.
     * @param passkey The passkey used to authenticate with the server.
     * 
     * @throws IllegalArgumentException if name does not validate.
     * @see #isValidUsername(String)
     * @see #VALID_USERNAME_DOC
     * @see #VALID_USERNAME_PATTERN
     */
    public User(String name, byte[] passkey) {
        this.username = name;
        if (passkey != null) this.passkey = passkey.clone();
        
        this.validateInstance();
    }

    /**
     * Gets this user's username.
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets this user's passkey to authenticate with.
     * @return
     */
    public byte[] getPasskey() {
    	// TODO think about security implications of public getPasskey
    	// Ideally the passkey would be erased from memory as soon as the user
    	// is authenticated, but that's infeasible to control precisely given
    	// Java's memory model anyway.
    	//
    	// TODO Perhaps we could have the passkey only be released in hashed form (even though it is shipped in clear)
        return passkey.clone();
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        
        // Check that our invariants are satisfied
        this.validateInstance();
    }
    
    // Check object invariants and do defensive copying.
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
