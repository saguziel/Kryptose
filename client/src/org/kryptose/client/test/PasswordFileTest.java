package org.kryptose.client.test;

import org.junit.*;
import org.kryptose.client.PasswordFile;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.requests.Blob;
import org.kryptose.requests.KeyDerivator;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PasswordFileTest {
	
	public static String MASTER_PWD = "MasterPassword";
	public static String MASTER_PWD_WRONG = "WrongMasterPassword";
	
	public static String USERNAME1 = "Antonio";
	public static String USERNAME1_WRONG = "WrongUsername";
	

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KeyDerivator.setParams("AAAAAAAAAAAAAAAA", 40);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

	/*
    @Test
	public final void blobTest() {
		
		fail("Not yet implemented"); // TODO
	}
*/

    @Test
    public final void blobEncryptionTest() throws Exception {
    	//Create a blob with a credential, encrypt, decrypt and verify it is unaltered.
        PasswordFile p = new PasswordFile(USERNAME1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(USERNAME1, MASTER_PWD, LocalDateTime.now());

        PasswordFile p2 = new PasswordFile(USERNAME1, b, MASTER_PWD);

    	assertEquals(p2.getVal("MyDom","MyUser") , "MyPwd");
    	assertNotEquals(p2.getVal("MyDom","MyUser") , "MyPwdWrong");


    }

    @Test(expected = CryptoErrorException.class)
    public final void blobDecryptWrongMasterPasswordTest() throws Exception {

        PasswordFile p = new PasswordFile(USERNAME1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(USERNAME1, MASTER_PWD, LocalDateTime.now());

        //Try to decrypt the blob with wrong password
        PasswordFile p2 = new PasswordFile(USERNAME1, b, MASTER_PWD_WRONG);
    }

    @Test(expected = CryptoErrorException.class)
    public final void blobDecryptWrongUsernameTest() throws Exception {

        PasswordFile p = new PasswordFile(USERNAME1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(USERNAME1, MASTER_PWD, LocalDateTime.now());

        //Try to decrypt the blob with wrong username
        PasswordFile p2 = new PasswordFile(USERNAME1_WRONG, b, MASTER_PWD);
    }


    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionTamperedBlobTest1() throws Exception {

        PasswordFile p = new PasswordFile(USERNAME1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(USERNAME1, MASTER_PWD, LocalDateTime.now());

        //Try to alter the blob
        byte[] enc = b.getEncBytes();
        if(enc[0] != 0)
        	enc[0] = 0;
        else 
        	enc[0] = 1;

        Blob b2 = new Blob(enc, b.getIv());

        assertFalse(Arrays.equals(b.getDigest(), b2.getDigest()));

        //This should fail, as the blob has been tampered.
        PasswordFile p2 = new PasswordFile(USERNAME1, b2, MASTER_PWD);
    }

    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionTamperedBlobTest2() throws Exception {

        PasswordFile p = new PasswordFile(USERNAME1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(USERNAME1, MASTER_PWD, LocalDateTime.now());

        //Try to alter the blob
        byte[] iv = b.getIv();
        if(iv[0] != 0)
        	iv[0] = 0;
        else 
        	iv[0]=1;

        Blob b2 = new Blob(b.getEncBytes(), iv);

        assertFalse(Arrays.equals(b.getDigest(), b2.getDigest()));
        
        //This should fail, as the blob has been tampered.
        PasswordFile p2 = new PasswordFile(USERNAME1, b2, MASTER_PWD);
    }

}
