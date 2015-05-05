package org.kryptose.server;

import org.kryptose.requests.KeyDerivator;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by alexguziel on 4/14/15.
 */
public class LogReader {
	private byte[] auth_key;


	public LogReader(byte[] auth_key) {
		this.auth_key = auth_key.clone();
	}

	public static void clearArray(char[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = 'a';
		}
	}

	public static void clearArray(byte[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = (byte) 0;
		}
	}

	public static char[] readToNewline(InputStreamReader in, int limit) {
		char[] buffer = new char[limit + 1];
		int pos = 0;
		try {
			// TODO: don't ignore return value.
			in.read(buffer, 0, limit + 1);
		} catch (IOException e) {
			System.out.println("error reading console input, unable to set up logs");
			System.exit(1);
		}
		while (pos <= limit) {
			if (buffer[pos] == '\n' || buffer[pos] == '\r') {
				break;
			}
			pos++;
		}
		if (pos == limit + 1) {
			System.out.println("String too long");
			System.exit(1);
		}
		char[] copy = Arrays.copyOf(buffer, pos);
		clearArray(buffer);
		return copy;
	}

	public static byte[] passwordToBytes(char[] password) {
		byte[] authkeywhole = KeyDerivator.getAuthenticationKeyBytes("admin", password);
		byte[] authkey = Arrays.copyOf(authkeywhole, 128 / 8);
		clearArray(authkeywhole);
		return authkey;

	}

	public static void main(String[] args) throws Exception {
		InputStreamReader in = new InputStreamReader(System.in);
		System.out.println("Enter CREATE to create password file or READ to read logs");
		String choice = new String(readToNewline(in, 100));
		KeyDerivator.setParams("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", 10);
		char[] password;
		byte[] authkey;
		switch (choice.toUpperCase()) {
		case "CREATE":
			System.out.println("Enter password (up to 100 char)");
			password = readToNewline(in, 100);

			authkey = passwordToBytes(password);
			Base64.Encoder encoder = Base64.getEncoder();
			byte[] encodedAuthKey = encoder.encode(authkey);
			clearArray(authkey);
			clearArray(password);

			Path p = FileSystems.getDefault().getPath("", "logPassfile");
			if (p.getParent() != null) {
				Files.createDirectories(p.getParent());
			}
			Files.write(p, encodedAuthKey, StandardOpenOption.CREATE_NEW);

			break;

		case "READ":
			System.out.println("Enter password up to 100 char");
			password = readToNewline(in, 100);
			authkey = passwordToBytes(password);
			clearArray(password);

			LogReader lr = new LogReader(authkey);
			String[] entries = new String[3];
			String[] temp = new String[1];

			BufferedWriter bw = null;

			int i = 0;
			outerloop:
				do {
					if (bw != null) {
						bw.close();
					}
					System.out.println("Enter log name or BREAK to quit");
					String logName = new String(readToNewline(in, 200));
					if (logName.equalsIgnoreCase("BREAK")) {
						break;
					}
					int counter = 0;
					int successful = 0;
					try (BufferedReader br = new BufferedReader(new FileReader("datastore/" + logName))) {


						bw = new BufferedWriter(new FileWriter("datastore/" + logName + ".out"));
						int iter_count = 1000;
						while (true) {
							if (i < 3) {
								entries[i] = br.readLine();
								if (entries[i] == null) {
									break;
								}
							} else {
								temp[0] = entries[0] + "\n" + entries[1] + "\n" + entries[2];
								if (counter == iter_count) {
									System.out.format("Tag match not found up to hash iteration %d, continue for how many more loops%n", counter);
									String continuestring = new String(readToNewline(in, 100));
									int x = 0;
									while (true) {
										try {
											x = Integer.parseInt(continuestring);
											iter_count += x;
											break;
										} catch (NumberFormatException e) {
											System.out.println("Enter a valid number");
											continuestring = new String(readToNewline(in, 100));
										}
									}
									if (x == 0) {
										break;
									}
								}
								String[] decoded = lr.decrypt(temp, counter++);
								if (decoded == null) {
									continue;
								} else {
									successful++;
									bw.write(decoded[0]);
								}
							}
							i = (i + 1) % 4;
						}
					} catch (FileNotFoundException e) {
						System.out.println("File not found, try again");
						continue;
					}
					System.out.format("Successfully wrote %d entries to %s%s%n", successful, logName, ".out");
				} while (true);
			if (bw != null) {
				bw.close();
			}
			break;
		default:
			System.out.println("Choice must be CREATE or READ");


		}

	}

	public String[] decrypt(String[] entries, int start) {

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		SecretKeySpec sks;
		Cipher c;

		String[] results = new String[entries.length];

		byte[] current_key = this.auth_key;

		for (int i = 0; i < start; i++) {
			md.reset();
			md.update("iterate".getBytes(Charset.forName("UTF-8")));
			md.update(current_key);
			current_key = md.digest();
			current_key = Arrays.copyOfRange(current_key, 0, 128 / 8);
		}

		for (int i = 0; i < entries.length; i++) {
			String[] split_s = entries[i].split("\n");
			if (split_s.length < 3) {
				continue;
			}

			byte[][] things = new byte[3][];
			for (int j = 0; j < split_s.length; j++) {
				things[j] = DatatypeConverter.parseHexBinary(split_s[j]);
			}


			md.reset();
			md.update("encrypt".getBytes(Charset.forName("UTF-8")));
			md.update(current_key);
			byte[] mk = Arrays.copyOfRange(md.digest(), 0, 128 / 8);
			sks = new SecretKeySpec(mk, "AES");
			byte[] output = null;
			byte[] tag = null;
			try {
				//need to add IV
				c = Cipher.getInstance("AES/CBC/PKCS5Padding");
				IvParameterSpec ivspec = new IvParameterSpec(things[0]);
				c.init(Cipher.DECRYPT_MODE, sks, ivspec);
				output = c.doFinal(things[1]);

				SecretKeySpec mac_key = new SecretKeySpec(current_key, "AES");
				Mac m = Mac.getInstance("HmacSHA1");
				m.init(mac_key);
				tag = m.doFinal(things[1]);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
					InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				//System.out.println(e.getMessage());
				return null;
			}
			if (Arrays.equals(tag, things[2])) {
				results[i] = new String(output, Charset.forName("UTF-8"));
			} else {
				//System.out.println("Tag mismatch");
				return null;
			}


			md.reset();
			md.update("iterate".getBytes(Charset.forName("UTF-8")));
			md.update(current_key);
			current_key = md.digest();
			current_key = Arrays.copyOfRange(current_key, 0, 128 / 8);

		}
		return results;
	}

	public void destroy() {
		Arrays.fill(auth_key, (byte) 0);
		auth_key = null;
	}

}
