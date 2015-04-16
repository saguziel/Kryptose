package org.kryptose.client;

import org.kryptose.requests.Log;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A set of credentials.
 *
 */
public class Credential implements Serializable {
	
	private String username;
	private String password;
	private String domain;
	private LocalDateTime lastmod;
	
	public Credential(String username, String password, String domain) {
		super();
		this.username = username;
		this.password = password;
		this.domain = domain;
        recordTime();
	}

    private void recordTime(){
        lastmod = LocalDateTime.now();
    }
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}


    public LocalDateTime getMod() {
        return this.lastmod;
    }

    public String getDomain() {
		return domain;
	}

    void setPassword(String p) {
        this.password = p;
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
