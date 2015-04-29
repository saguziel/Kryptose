package org.kryptose.client;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.logging.Level;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kryptose.client.RequestHandler;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.requests.Request;
//import org.kryptose.server.FatalError;


public class RequestHandlerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

	@Test(expected = IOException.class)
	public void testSendWhenNoServerIsListening() throws Exception{
		RequestHandler r = new RequestHandler("localhost",5002,"ClientTrustStore.jks","aaaaaa");		
		Request req = mock(Request.class);		
		r.send(req);		
	}
		

}
