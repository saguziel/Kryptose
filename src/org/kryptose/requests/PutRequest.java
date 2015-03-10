package org.kryptose.requests;

import org.kryptose.server.DataStore;

public class PutRequest extends Request {

	// TODO generate serialversionuid after fields are decided upon
	
	private Blob blob;
	
	public PutRequest(Blob blob) {
		this.blob = blob;
	}
	
	@Override
	public void run() {
		DataStore dataStore = this.getServer().getDataStore();
		dataStore.writeBlob(this.getUser(), this.blob);
	}

}
