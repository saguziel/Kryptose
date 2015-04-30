package org.kryptose.common;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kryptose.requests.KeyDerivator;

public class KeyDerivatorTest {
	// We assume the implementation of PBKDF2 is correct, as well as the one of the underlying hash functions. We will just test that
	// the different parts of the input to those functions affect the output
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KeyDerivator.setParams("AAAAAAAAAAAAAAAA", 40);
    }

	@Test
	public final void testKeyDerivator() throws Exception{
		String USERNAME = "Antonio";
		char[] PASSWORD = "MyPwd".toCharArray();
		String USERNAME_WRONG = "AntonioWRONG";
		char[] PASSWORD_WRONG = "MyPwdWRONG".toCharArray();
		
		byte[] realAuthKey = KeyDerivator.getAuthenticationKeyBytes(USERNAME, PASSWORD);
		byte[] realEncKey = KeyDerivator.getEncryptionKeyBytes(USERNAME, PASSWORD);
	
		assertArrayEquals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME, PASSWORD));
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME_WRONG, PASSWORD)));
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME, PASSWORD_WRONG)));
		assertArrayEquals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME, PASSWORD));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME_WRONG, PASSWORD)));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME, PASSWORD_WRONG)));

		//Let's change the initial parameters (like the Application-wide salt) and verify output changes
        KeyDerivator.setParams("AAAAAAAABBBBBBBB", 40);
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME, PASSWORD)));
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME, PASSWORD_WRONG)));
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME_WRONG, PASSWORD)));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME, PASSWORD)));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME_WRONG, PASSWORD)));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME, PASSWORD_WRONG)));

        KeyDerivator.setParams("AAAAAAAAAAAAAAAA", 42);
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME, PASSWORD)));
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME, PASSWORD_WRONG)));
		assertFalse(Arrays.equals(realAuthKey, KeyDerivator.getAuthenticationKeyBytes(USERNAME_WRONG, PASSWORD)));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME, PASSWORD)));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME_WRONG, PASSWORD)));
		assertFalse(Arrays.equals(realEncKey, KeyDerivator.getEncryptionKeyBytes(USERNAME, PASSWORD_WRONG)));

	}
}
