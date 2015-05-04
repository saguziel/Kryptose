package org.kryptose.client;

import org.junit.*;
import static org.junit.Assert.*;

import org.kryptose.client.Credential;
import org.kryptose.client.PasswordFile;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.requests.Blob;
import org.kryptose.requests.KeyDerivator;

import java.time.LocalDateTime;
import java.util.Arrays;


public class PasswordFileTest {

	public static char[] MASTER_PWD = "MasterPassword".toCharArray();
	public static char[] MASTER_PWD_WRONG = "WrongMasterPassword".toCharArray();

	public static String USERNAME1 = "antonio";
	public static String USERNAME1_WRONG = "wrongusername";

    public static MasterCredentials MASTER_CRED_1;
	public static MasterCredentials MASTER_CRED_1_WRONG_PWD;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KeyDerivator.setParams("AAAAAAAAAAAAAAAA", 40);
        MASTER_CRED_1 = new MasterCredentials(USERNAME1, MASTER_PWD);
    	MASTER_CRED_1_WRONG_PWD = new MasterCredentials(USERNAME1, MASTER_PWD_WRONG);


    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        KeyDerivator.setParams("AAAAAAAAAAAAAAAA", 40);

    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public final void blobEncryptionTest() throws Exception {
    	//Create a blob with a credential, encrypt, decrypt and verify it is unaltered.
        PasswordFile p = new PasswordFile(MASTER_CRED_1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(LocalDateTime.now());

        PasswordFile p2 = new PasswordFile(MASTER_CRED_1, b);

    	assertEquals(p2.getVal("MyDom","MyUser") , "MyPwd");
    	assertNotEquals(p2.getVal("MyDom","MyUser") , "MyPwdWrong");


    }

    @Test
    public final void PasswordFileOperationsTest() throws Exception {
    	//Create a blob with a credential, encrypt, decrypt and verify it is unaltered.
        PasswordFile p = new PasswordFile(MASTER_CRED_1);
        p.setVal("MyDom", "MyUser", "MyPwd");
        p.setVal("MyDom2", "MyUser", "MyPwd");
        //This should not create a new credential, but update the above one.
        p.setVal("MyDom2", "MyUser", "MyPwd2");

        assertNull(p.getVal(-5));
        assertNull(p.getVal(2));
        assertNull(p.delVal(-2));
        Credential c = new Credential("MyUser", "MyPwd", "MyDom");
        assertEquals(c.getPassword(), p.getVal("MyDom","MyUser"));
        assertTrue(c.equals(p.getVal(0)));
        p.delVal(0);
        assertFalse(c.equals(p.getVal(0)));


    }


    @Test(expected = CryptoErrorException.class)
    public final void blobDecryptWrongMasterPasswordTest() throws Exception {

        PasswordFile p = new PasswordFile(MASTER_CRED_1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(LocalDateTime.now());

        //Try to decrypt the blob with wrong password
        new PasswordFile(MASTER_CRED_1_WRONG_PWD, b);
    }

    @Test(expected = CryptoErrorException.class)
    public final void blobDecryptWrongUsernameTest() throws Exception {

        PasswordFile p = new PasswordFile(MASTER_CRED_1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(LocalDateTime.now());

        //Try to decrypt the blob with wrong username
        new PasswordFile(new MasterCredentials(USERNAME1_WRONG, MASTER_PWD), b);
    }


    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionTamperedBlobTest1() throws Exception {

        PasswordFile p = new PasswordFile(MASTER_CRED_1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(LocalDateTime.now());

        //Try to alter the blob
        byte[] enc = b.getEncBytes();
        if(enc[0] != 0)
        	enc[0] = 0;
        else
        	enc[0] = 1;

        Blob b2 = new Blob(enc, b.getIv());

        assertFalse(Arrays.equals(b.getDigest(), b2.getDigest()));

        //This should fail, as the blob has been tampered.
        new PasswordFile(MASTER_CRED_1, b2);
    }

    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionTamperedBlobTest2() throws Exception {

        PasswordFile p = new PasswordFile(MASTER_CRED_1);
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob(LocalDateTime.now());

        //Try to alter the blob
        byte[] iv = b.getIv();
        if(iv[0] != 0)
        	iv[0] = 0;
        else
        	iv[0]=1;

        Blob b2 = new Blob(b.getEncBytes(), iv);

        assertFalse(Arrays.equals(b.getDigest(), b2.getDigest()));

        //This should fail, as the blob has been tampered.
        new PasswordFile(MASTER_CRED_1, b2);
    }

}
