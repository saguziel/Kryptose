package org.kryptose.client;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.security.auth.Destroyable;

import org.kryptose.Utils;

/**
 * A set of credentials.
 *
 */
public class Credential implements Serializable, Destroyable {
	private static final long serialVersionUID = 7942387805138241646L;
	
	private String username;
	private char[] password;
	private String domain;
	private LocalDateTime lastmod;

    // destroys the input password char[]
	public Credential(String username, char[] password, String domain) {
		super();
		this.username = username;
		this.password = password.clone();
		Utils.destroyPassword(password);
		this.domain = domain;
        recordTime();
	}

    private void recordTime(){
        lastmod = LocalDateTime.now();
    }
	
	public String getUsername() {
		return username;
	}

	public char[] getPasswordClone() {
		return password == null ? null : password.clone();
	}


    public LocalDateTime getMod() {
        return this.lastmod;
    }

    public String getDomain() {
		return domain;
	}

    // destroys the input password char[]
    void setPassword(char[] p) {
    	Utils.destroyPassword(this.password);
        this.password = p.clone();
        Utils.destroyPassword(p);
        recordTime();
    }

    void setUsername(String u){
        this.username = u;
        recordTime();
    }

    void setDomain(String d){
        this.domain = d;
        recordTime();
    }
	
    public void destroy() {
    	Utils.destroyPassword(this.password);
    }
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Credential other = (Credential) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!Arrays.equals(password,other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
