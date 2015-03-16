package org.kryptose.client;
import org.kryptose.requests.Blob;

import java.util.ArrayList;

/**
 * Created by jeff on 3/15/15.
 */
public interface BlobParser {

    public ArrayList<Credential> decryptBlob(String key, Blob b);
    public Blob encryptBlob(String key, ArrayList<Credential> l);

}
