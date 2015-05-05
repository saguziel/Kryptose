package org.kryptose.client;

import org.kryptose.Utils;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.Blob;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Destroyable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jeff on 3/15/15.
 */
public class PasswordFile implements Destroyable {

    ArrayList<Credential> credentials;
    LocalDateTime timestamp;
    
    transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    MasterCredentials mCred = null;
    byte[] oldDigest;

    public PasswordFile(MasterCredentials mCred, Blob b) throws BadBlobException, CryptoPrimitiveNotSupportedException, CryptoErrorException {
        //String password = new String(mCred.getPassword());
        this.mCred = mCred;
    	decryptBlob(b);
        // TODO use char array for passwords
    }

    
    public PasswordFile(MasterCredentials mCred) {
    	this.mCred = mCred;
        this.timestamp = LocalDateTime.now(); // TODO: this timestamp is never used?
        this.credentials = new ArrayList<Credential>();
    }


    private static Blob rawBlobCreate(byte[] raw_data, byte[] raw_key) throws CryptoPrimitiveNotSupportedException, CryptoErrorException {
        Blob b;

        try {
            Cipher c;
            c = Cipher.getInstance("AES/GCM/NoPadding");
            final int blockSize = c.getBlockSize();

            final byte[] ivData = new byte[blockSize];
            final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
            rnd.nextBytes(ivData);

            GCMParameterSpec params = new GCMParameterSpec(blockSize * Byte.SIZE, ivData);

            SecretKeySpec sks = new SecretKeySpec(raw_key, "AES");
            c.init(Cipher.ENCRYPT_MODE, sks, params);

            b = new Blob(c.doFinal(raw_data), ivData);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoPrimitiveNotSupportedException(e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoErrorException(e);
        }

        return b;
    }

    private static byte[] rawBlobDecrypt(Blob b, byte[] raw_key) throws CryptoPrimitiveNotSupportedException, CryptoErrorException {

        try {
            Cipher c;
            c = Cipher.getInstance("AES/GCM/NoPadding");
            final int blockSize = c.getBlockSize();

            GCMParameterSpec params = new GCMParameterSpec(blockSize * Byte.SIZE, b.getIv());


            SecretKeySpec sks = new SecretKeySpec(raw_key, "AES");
            c.init(Cipher.DECRYPT_MODE, sks, params);

            return c.doFinal(b.getEncBytes());

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoPrimitiveNotSupportedException(e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            //Note: this can be due to a tampered blob or malicious attack
            throw new CryptoErrorException(e);
        }
    }

    public byte[] getOldDigest() {
        if (oldDigest == null) return null;
        return oldDigest.clone();
    }

    public void setOldDigest(byte[] digest){
        if (digest == null) {
            this.oldDigest = null;
        } else {
            this.oldDigest = digest.clone();
        }
    }

    @SuppressWarnings("unchecked")
	public void decryptBlob(Blob b) throws BadBlobException, CryptoPrimitiveNotSupportedException, CryptoErrorException {
        if (mCred == null){
        	throw new BadBlobException("credentials for Blob decryption not available");
        }
    	//byte[] raw_key = KeyDerivator.getEncryptionKeyBytes(mCred.getUsername(), mCred.getPassword());

        byte[] decrypted = rawBlobDecrypt(b, mCred.getCryptKey());
    	//TODO ALEX: destroy raw_key;

        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(decrypted);
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            credentials = (ArrayList<Credential>) objStream.readObject();
            for (Credential cred : credentials) {
            	Credential.class.cast(cred);
            }
            timestamp = (LocalDateTime) objStream.readObject();
        } catch (ClassNotFoundException | ClassCastException | IOException e) {
            throw new BadBlobException("Bad blob");
        }
    }

	public Blob encryptBlob(LocalDateTime lastModDate) throws CryptoPrimitiveNotSupportedException, BadBlobException, CryptoErrorException {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(credentials);
            objStream.writeObject(lastModDate);
            objStream.flush();
            byte[] bytes = byteStream.toByteArray();
            objStream.close();
       
            if (mCred==null) {
            	throw new BadBlobException("Trying to encrypt a Blob without an encryption key. Something is wrong in the code.");
            }
            
            return rawBlobCreate(bytes,mCred.getCryptKey());

        } catch (IOException e) {
            e.printStackTrace();
            throw new BadBlobException("Bad blob");
        }
	}

    
    public Credential getVal(int index) {
        if (0 <= index && index < credentials.size()) {
            Credential c = credentials.get(index);
            return c;
        }
        return null;
    }

    public char[] getValClone(String dom, String user) {
        for (Credential c : credentials) {
            if (c.getDomain().equals(dom) && c.getUsername().equals(user)) {
                return c.getPasswordClone();
            }
        }
        return null;
    }

    // returns true if value overwritten, false if new val inserted
    public Boolean setVal(String dom, String user, char[] pass) {
        for (Credential c : credentials) {
            if (c.getDomain().equals(dom) && c.getUsername().equals(user)) {
            	char[] oldVal = c.getPasswordClone();
                // Credential class now responsible for destroying pass.
                c.setPassword(pass);
                changeSupport.firePropertyChange(user + "@" + dom, oldVal, pass);
                Utils.destroyPassword(oldVal);
                return true;
            }
        }
        // Credential class now responsible for destroying pass.
        credentials.add(new Credential(user, pass, dom));
        changeSupport.firePropertyChange(user + "@" + dom, null, pass);
        return false;
    }

    public Credential delVal(int index) {
        if (0 <= index && index < credentials.size()) {
            Credential c = credentials.get(index);
            String dom = c.getDomain();
            String user = c.getUsername();
            char[] oldVal = c.getPasswordClone();
            credentials.remove(index);
            changeSupport.firePropertyChange(user + "@" + dom, oldVal, null);
            Utils.destroyPassword(oldVal);
            return c;
        }
        return null;
    }

    // returns true iff value associated w/ dom successfully deleted
    public Boolean delVal(String dom, String user) {
        int toRem = -1;
        for (int i = 0; i < credentials.size(); i++) {
            if (credentials.get(i).getDomain().equals(dom) && credentials.get(i).getUsername().equals(user)) {
                toRem = i;
                break;
            }
        }
        if (toRem >= 0) {
        	delVal(toRem);
            return true;
        }
        return false;
    }

    public ArrayList<Credential> toList() {
        return credentials;
    }

    public static class BadBlobException extends Exception {
		private static final long serialVersionUID = 4063553143136409234L;

		public BadBlobException(String message) {
            super(message);
        }
    }

	public void destroy() {
		mCred.destroy();
		for (Credential c : credentials){
			c.destroy();
		}
	}
    
	public void addChangeListener(PropertyChangeListener listener) {
		this.changeSupport.addPropertyChangeListener(listener);
	}

	
	public boolean existsCredential(String domain, String username){
		if(this.credentials == null || username == null || domain == null) return false;
		
		for (Credential cred : this.credentials) {
			if(username.equals(cred.getUsername()) && domain.equals(cred.getDomain()))
				return true;
		}
		return false;
	}
	
	public String[] getDomains() {
		Set<String> domains = new HashSet<String>();
		for (Credential cred : this.credentials) {
			domains.add(cred.getDomain());
		}
		return domains.toArray(new String[domains.size()]);
	}

	public String[] getUsernames(String domain) {
		Set<String> usernames = new HashSet<String>();
		for (Credential cred : this.credentials) {
			if (cred.getDomain().equals(domain)) {
				usernames.add(cred.getUsername());
			}
		}
		return usernames.toArray(new String[usernames.size()]);
	}
    
	public Blob encryptBlob(MasterCredentials mCred, LocalDateTime lastModDate) throws CryptoPrimitiveNotSupportedException, BadBlobException, CryptoErrorException {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(credentials);
            objStream.writeObject(lastModDate);
            objStream.flush();
            byte[] bytes = byteStream.toByteArray();
            objStream.close();
       
            return rawBlobCreate(bytes,mCred.getCryptKey());

        } catch (IOException e) {
            e.printStackTrace();
            throw new BadBlobException("Bad blob");
        }
	}

	
}
