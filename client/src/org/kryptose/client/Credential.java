package org.kryptose.client;


import java.io.Serializable;
import java.time.LocalDateTime;

import javax.security.auth.Destroyable;

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
	
	public Credential(String username, char[] password, String domain) {
		super();
		this.username = username;
		this.password = password.clone();
		this.domain = domain;
        recordTime();
	}

    private void recordTime(){
        lastmod = LocalDateTime.now();
    }
	
	public String getUsername() {
		return username;
	}

	public char[] getPassword() {
		return password;
	}


    public LocalDateTime getMod() {
        return this.lastmod;
    }

    public String getDomain() {
		return domain;
	}

    void setPassword(char[] p) {
        this.password = p.clone();
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
    	// TODO destroy credentials
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
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
