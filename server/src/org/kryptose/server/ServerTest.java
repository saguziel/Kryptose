package org.kryptose.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kryptose.exceptions.InvalidCredentialsException;
import org.kryptose.exceptions.StaleWriteException;
import org.kryptose.exceptions.UsernameInUseException;
import org.kryptose.requests.*;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by alexguziel on 5/4/15.
 */
public class ServerTest {
    public static Server server;
    public static User user;
    public static String username = "jeff";
    public static byte[] password = "jeff".getBytes();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        File file = new File("datastore/usertable.bin");
        if (file != null) file.delete();
        server = new Server();
        Thread t1 = new Thread(
                new Runnable() {
                    public void run() {
                        server.start();
                    }
                }
        );
        t1.start();
        user = new User(username, password);
        Thread.sleep(5000);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        File file = new File("datastore/usertable.bin");
        if (file != null) file.delete();
        File[] files = new File("datastore/blobs").listFiles();
        for (File f : files) {
            f.delete();
        }
    }

    public static Blob randomBlob() {
        byte[] enc = new byte[40];
        byte[] iv = new byte[20];
        new Random().nextBytes(enc);
        new Random().nextBytes(iv);
        return new Blob(iv, enc);
    }

    @Test
    public void testCreateAccountSuccess() throws Exception {
        Request request;
        Response response;
        request = new RequestCreateAccount(user);
        response = server.handleRequest(request);
        assert (response instanceof ResponseCreateAccount);
        ((ResponseCreateAccount) response).verifySuccessful();

    }

    @Test
    public void testCreateAccountUsernameInUse() throws Exception {
        Request request;
        Response response;
        User u = new User("eggnold", password);
        request = new RequestCreateAccount(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseCreateAccount);
        ((ResponseCreateAccount) response).verifySuccessful();
        response = server.handleRequest(request);
        try {
            ((ResponseCreateAccount) response).verifySuccessful();
            assert false;
        } catch (UsernameInUseException e) {
            return;
        }
    }

    @Test
    public void testChangePassword() throws Exception {
        Request request;
        Response response;
        byte[] newPassword = new byte[20];
        new Random().nextBytes(newPassword);
        User u = new User("jeff231", password);
        request = new RequestCreateAccount(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseCreateAccount);
        ((ResponseCreateAccount) response).verifySuccessful();


        request = new RequestChangePassword(u, newPassword, null, null);
        response = server.handleRequest(request);
        assert (response instanceof ResponseChangePassword);
        ((ResponseChangePassword) response).getDigest();

        u = new User(u.getUsername(), newPassword);

        new Random().nextBytes(newPassword);
        Blob b = randomBlob();
        request = new RequestPut(u, b, null);
        server.handleRequest(request);
        Blob b2 = randomBlob();
        request = new RequestChangePassword(u, newPassword, b2, b.getDigest());
        response = server.handleRequest(request);
        assert (response instanceof ResponseChangePassword);
        ((ResponseChangePassword) response).getDigest();

        u = new User(u.getUsername(), newPassword);

        new Random().nextBytes(newPassword);
        request = new RequestChangePassword(u, newPassword, null, null);
        response = server.handleRequest(request);
        assert (response instanceof ResponseChangePassword);
        try {
            System.out.format("%s\n", new String(((ResponseChangePassword) response).getDigest()));
            assert (false);
        } catch (StaleWriteException e) {

        }


    }

    @Test
    public void testAuthentication() throws Exception {
        Request request;
        Response response;
        Blob data = randomBlob();

        User u = new User("nimit", password);
        request = new RequestCreateAccount(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseCreateAccount);
        ((ResponseCreateAccount) response).verifySuccessful();

        User u2 = new User("nimit", null);
        User u3 = new User("nimit", Arrays.copyOfRange(password, 0, 10));

        request = new RequestGet(u2);
        response = server.handleRequest(request);
        assert (response instanceof ResponseErrorReport);

        request = new RequestGet(u3);
        response = server.handleRequest(request);
        assert (response instanceof ResponseErrorReport);
    }

    @Test
    public void testGet() throws Exception {
        Request request;
        Response response;
        Blob data = randomBlob();

        User u = new User("yechuan", password);
        request = new RequestCreateAccount(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseCreateAccount);
        ((ResponseCreateAccount) response).verifySuccessful();

        request = new RequestGet(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseGet);
        assert (((ResponseGet) response).getBlob() == null);

        //put data
        RequestPut rp = new RequestPut(u, data, null);
        response = server.handleRequest(rp);
        assert (response instanceof ResponsePut);
        ((ResponsePut) response).getDigest();

        //put data again
        Blob data2 = randomBlob();
        rp = new RequestPut(u, data2, data.getDigest());
        response = server.handleRequest(rp);
        assert (response instanceof ResponsePut);
        ((ResponsePut) response).getDigest();

        request = new RequestGet(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseGet);
        assert (Arrays.equals(((ResponseGet) response).getBlob().getDigest(), data2.getDigest()));


    }

    @Test
    public void testPut() throws Exception {
        Request request;
        Response response;
        Blob data = randomBlob();

        User u = new User("yechuantian", password);
        request = new RequestCreateAccount(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseCreateAccount);
        ((ResponseCreateAccount) response).verifySuccessful();
        //put data
        RequestPut rp = new RequestPut(u, data, null);
        response = server.handleRequest(rp);
        assert (response instanceof ResponsePut);
        ((ResponsePut) response).getDigest();

        //put data again
        Blob data2 = randomBlob();
        rp = new RequestPut(u, data2, data.getDigest());
        response = server.handleRequest(rp);
        assert (response instanceof ResponsePut);
        ((ResponsePut) response).getDigest();

        //stale write
        Blob data3 = randomBlob();
        rp = new RequestPut(u, data3, data.getDigest());
        response = server.handleRequest(rp);
        assert (response instanceof ResponsePut);
        try {
            ((ResponsePut) response).getDigest();
            assert (false);
        } catch (StaleWriteException e) {

        }

        byte[] partial = new byte[1];
        partial[0] = data2.getDigest()[0];
        rp = new RequestPut(u, data, partial);
        response = server.handleRequest(rp);
        assert (response instanceof ResponsePut);
        try {
            ((ResponsePut) response).getDigest();
            assert (false);
        } catch (StaleWriteException e) {

        }


    }

    @Test
    public void testDelete() throws Exception {
        Request request;
        Response response;
        User u = new User("jeff69", password);
        request = new RequestCreateAccount(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseCreateAccount);
        ((ResponseCreateAccount) response).verifySuccessful();

        Blob b = randomBlob();
        request = new RequestPut(u, b, null);
        response = server.handleRequest(request);
        assert (response instanceof ResponsePut);
        ((ResponsePut) response).getDigest();

        request = new RequestDeleteAccount(u);
        response = server.handleRequest(request);
        assert (response instanceof ResponseDeleteAccount);
        assert (((ResponseDeleteAccount) response).verifySuccessful());

        File f = new File("datastore/blobs/jeff69.blob");
        assert (!f.exists());

        request = new RequestGet(u);
        server.handleRequest(request);
        response = server.handleRequest(request);
        assert (response instanceof ResponseErrorReport);
        assert (((ResponseErrorReport) response).getException() instanceof InvalidCredentialsException);


    }
}
