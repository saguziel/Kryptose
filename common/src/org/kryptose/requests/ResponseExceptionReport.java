package org.kryptose.requests;

import org.kryptose.exceptions.InternalServerErrorException;
import org.kryptose.exceptions.InvalidCredentialsException;
import org.kryptose.exceptions.MalformedRequestException;

public class ResponseExceptionReport extends Response {
	
	private InvalidCredentialsException icex = null;
	private MalformedRequestException mrex = null;
	private InternalServerErrorException iseex = null; 

	
    public ResponseExceptionReport(InvalidCredentialsException ex) {
		super();
		this.icex = ex;
	}
	
    public ResponseExceptionReport(MalformedRequestException ex) {
		super();
		this.mrex = ex;
	}
	
    public ResponseExceptionReport(InternalServerErrorException ex) {
		super();
		this.iseex = ex;
	}
    
    public Exception getException() {
    	if (this.iseex != null) {
    		return this.iseex;
    	}
    	if (this.mrex != null) {
    		return this.mrex;
    	}
    	if (this.icex != null) {
    		return this.icex;
    	}
    	return null;
    }
    
    @Override
    public void checkException() throws InternalServerErrorException,
									MalformedRequestException,
									InvalidCredentialsException {
    	if (this.iseex != null) {
    		throw this.iseex;
    	}
    	if (this.mrex != null) {
    		throw this.mrex;
    	}
    	if (this.icex != null) {
    		throw this.icex;
    	}
    }
	
	@Override
	public String logEntry() {
		// TODO: this is lazy.
		return "RESPONSE: " + this.getException().toString();
	}

}
