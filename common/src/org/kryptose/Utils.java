package org.kryptose;

import java.util.Arrays;

public class Utils {

	public static void destroyPassword(char[] password) {
		if (password != null) Arrays.fill(password, ' ');
	}

	public static void destroyPasskey(byte[] passkey) {
		if (passkey != null) Arrays.fill(passkey, (byte)0);
	}
	
}
