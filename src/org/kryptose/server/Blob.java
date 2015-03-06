package org.kryptose.server;

import java.io.Serializable;

/**
 * Contains all the encrypted information stored here by a single client.
 * 
 * @author jshi
 */
public class Blob implements Serializable {

	// TODO: generate serial version UID, after fields are decided.
	
	byte[] blob;
	
}
