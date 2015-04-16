package org.kryptose.client.test;

import org.junit.*;
import org.kryptose.client.PasswordFile;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.requests.Blob;
import org.kryptose.requests.KeyDerivator;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;

public class PasswordFileTest {

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

        PasswordFile p = new PasswordFile("Antonio");
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob("Antonio", "MasterPassword", LocalDateTime.now());

        PasswordFile p2 = new PasswordFile("Antonio", b, "MasterPassword");

//    	assertEquals(p2.getVal("MyUser"),"MyPwd");

    }

    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionBadParametersTest() throws Exception {

        PasswordFile p = new PasswordFile("Antonio");
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob("Antonio", "MasterPassword", LocalDateTime.now());

        //Try to decrypt the blob with wrong password
        PasswordFile p2 = new PasswordFile("Antonio", b, "MasterPasswordAAA");
    }

    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionBadParametersTest1() throws Exception {

        PasswordFile p = new PasswordFile("Antonio");
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob("Antonio", "MasterPassword", LocalDateTime.now());

        //Try to decrypt the blob with wrong password
        PasswordFile p2 = new PasswordFile("AntonioA", b, "MasterPassword");
    }


    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionBadBlobTest1() throws Exception {

        PasswordFile p = new PasswordFile("Antonio");
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob("Antonio", "MasterPassword", LocalDateTime.now());

        //Try to alter the blob
        byte[] enc = b.getEncBytes();
        enc[0] = 0;

        Blob b2 = new Blob(enc, b.getIv());

        assertFalse(Arrays.equals(b.getDigest(), b2.getDigest()));


        PasswordFile p2 = new PasswordFile("AntonioA", b2, "MasterPassword");
    }

    @Test(expected = CryptoErrorException.class)
    public final void blobEncryptionBadBlobTest2() throws Exception {

        PasswordFile p = new PasswordFile("Antonio");
        p.setVal("MyDom", "MyUser", "MyPwd");

        Blob b = p.encryptBlob("Antonio", "MasterPassword", LocalDateTime.now());

        //Try to alter the blob
        byte[] iv = b.getIv();
        iv[0] = 0;

        Blob b2 = new Blob(b.getEncBytes(), iv);

        assertFalse(Arrays.equals(b.getDigest(), b2.getDigest()));

        PasswordFile p2 = new PasswordFile("AntonioA", b2, "MasterPassword");
    }

}
