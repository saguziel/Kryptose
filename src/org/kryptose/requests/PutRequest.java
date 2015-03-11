package org.kryptose.requests;

public class PutRequest extends Request {

	// TODO generate serialversionuid after fields are decided upon
	
	private final Blob blob;
	
	public PutRequest(Blob blob) {
		this.blob = blob;
	}
	
	public Blob getBlob() {
		return blob;
	}

}
