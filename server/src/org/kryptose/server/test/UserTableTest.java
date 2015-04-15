//package org.kryptose.server.test;
//
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.logging.Logger;
//
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.kryptose.requests.User;
//import org.kryptose.server.UserTable;
//import org.kryptose.server.UserTable.Result;
//
///**
// *
// * @author jshi
// */
//public class UserTableTest {
//
//	private static Logger LOGGER = Logger.getLogger(UserTableTest.class.getCanonicalName());
//	private static final String TEST_FILE = "test-usertable.bin";
//	private static final String TEST_USERNAME = "testuser";
//	private static final String TEST_USERNAME_2 = "testuser2";
//	private static final byte[] TEST_PASSKEY = new byte[] {0, 1, 2, 3, 4, 5, 6, 7};
//	private static final byte[] TEST_PASSKEY_2 = new byte[] {0, 1, 1, 2, 3, 5, 8, 13};
//	private static final byte[] TEST_BAD_PASSKEY = new byte[] {0, 1, 2, 3, 4, 5, 6, 6};
//	private static final User TEST_USER = new User(TEST_USERNAME, TEST_PASSKEY);
//	private static final User TEST_BAD_USER = new User(TEST_USERNAME, TEST_BAD_PASSKEY);
//	private static final User TEST_USER_2 = new User(TEST_USERNAME_2, TEST_PASSKEY_2);
//
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//	}
//
//	@Before
//	public void setUp() throws Exception {
//		new File(TEST_FILE).delete();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		new File(TEST_FILE).delete();
//	}
//
//	@Test
//	public void testConstructor() {
//		new UserTable(LOGGER, TEST_FILE);
//	}
//
//	@Test
//	public void testCanPersist() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		ut.ensurePersist();
//	}
//
//	@Test
//	public void testCanPersistMultiple() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		Thread[] t = new Thread[50];
//		for (int i = 0; i < t.length; i++) {
//			t[i] = ut.ensurePersistNewThread();
//		}
//		ut.ensurePersist();
//		for (int i = 0; i < t.length; i++) {
//			try {
//				t[i].join();
//			} catch (InterruptedException e) {
//				fail("Thread interrupted.");
//			}
//		}
//	}
//
//	@Test
//	public void testReadBackEmpty() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		ut.ensurePersist();
//		try {
//			UserTable ut2 = UserTable.loadFromFile(LOGGER, TEST_FILE);
//		} catch (IOException e) {
//			fail(e.toString());
//		}
//	}
//	@Test
//	public void testAddUser() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		assertEquals(ut.addUser(TEST_USER), Result.USER_ADDED);
//	}
//
//	@Test
//	public void testAuthPass() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		ut.addUser(TEST_USER);
//		assertEquals(ut.auth(TEST_USER), Result.AUTHENTICATION_SUCCESS);
//	}
//
//	@Test
//	public void testAuthFail() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		ut.addUser(TEST_USER);
//		assertEquals(ut.auth(TEST_BAD_USER), Result.WRONG_CREDENTIALS);
//	}
//
//	@Test
//	public void testAuthFailUserNotFound() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		ut.addUser(TEST_USER);
//		assertEquals(ut.auth(TEST_USER_2), Result.USER_NOT_FOUND);
//	}
//
//	@Test
//	public void testAddUserTwice() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		ut.addUser(TEST_USER);
//		assertEquals(ut.addUser(TEST_USER), Result.USER_ALREADY_EXISTS);
//	}
//
//	@Test
//	public void testAddTwoUsers() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		assertEquals(ut.addUser(TEST_USER), Result.USER_ADDED);
//		assertEquals(ut.addUser(TEST_USER_2), Result.USER_ADDED);
//	}
//
//	public void testContainsEmpty() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		assertFalse(ut.contains(TEST_USERNAME));
//		assertFalse(ut.contains(TEST_USERNAME_2));
//	}
//
//	public void testContainsOneUser() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		assertEquals(ut.addUser(TEST_USER), Result.USER_ADDED);
//		assertTrue(ut.contains(TEST_USERNAME));
//		assertFalse(ut.contains(TEST_USERNAME_2));
//	}
//
//	@Test
//	public void testReadBackOneUser() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		assertEquals(ut.addUser(TEST_USER), Result.USER_ADDED);
//		ut.ensurePersist();
//		try {
//			UserTable ut2 = UserTable.loadFromFile(LOGGER, TEST_FILE);
//			assertTrue(ut.contains(TEST_USERNAME));
//			assertFalse(ut.contains(TEST_USERNAME_2));
//			assertEquals(ut.auth(TEST_USER), Result.AUTHENTICATION_SUCCESS);
//			assertEquals(ut.auth(TEST_BAD_USER), Result.WRONG_CREDENTIALS);
//		} catch (IOException e) {
//			fail(e.toString());
//		}
//	}
//
//	@Test
//	public void testChangeAuthKey() {
//		UserTable ut = new UserTable(LOGGER, TEST_FILE);
//		assertEquals(ut.addUser(TEST_BAD_USER), Result.USER_ADDED);
//		assertEquals(ut.auth(TEST_BAD_USER), Result.AUTHENTICATION_SUCCESS);
//		assertEquals(ut.auth(TEST_USER), Result.WRONG_CREDENTIALS);
//		assertEquals(ut.changeAuthKey(TEST_USERNAME, TEST_BAD_PASSKEY, TEST_PASSKEY), Result.AUTH_KEY_CHANGED);
//		assertEquals(ut.auth(TEST_BAD_USER), Result.WRONG_CREDENTIALS);
//		assertEquals(ut.auth(TEST_USER), Result.AUTHENTICATION_SUCCESS);
//	}
//
//}
