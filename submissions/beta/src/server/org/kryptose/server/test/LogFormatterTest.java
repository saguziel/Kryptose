package org.kryptose.server.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kryptose.server.LogFormatter;
import org.kryptose.server.LogReader;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by alexguziel on 4/14/15.
 */
public class LogFormatterTest {
    private LogFormatter lf;
    private Formatter f;
    private byte[] auth_key;
    private LogReader lr;

    @Before
    public void setUp() throws Exception {
        auth_key = new byte[128 / 8];
        for (int i = 0; i < auth_key.length; i++) {
            auth_key[i] = (byte) i;
        }
        f = new SimpleFormatter();
        lf = new LogFormatter(new SimpleFormatter(), auth_key);
        lr = new LogReader(auth_key);
    }

    @After
    public void tearDown() throws Exception {
        auth_key = null;
        lf = null;
        f = null;
        lr = null;
    }

    @Test
    public void testFormat() throws Exception {
        LogRecord hello = new LogRecord(Level.SEVERE, "hello");
        String[] formatted = new String[10];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = lf.format(hello);
        }
        for (int i = 0; i < formatted.length - 1; i++) {
            assertNotEquals(formatted[i], formatted[i + 1]);
        }
        String simpleFormatted = f.format(hello);
        String[] decrypted = lr.decrypt(formatted);
        for (int i = 0; i < formatted.length; i++) {
            assertEquals(decrypted[i], simpleFormatted);
        }
    }

    @Test
    public void writeBytes() throws Exception {
        Path p = FileSystems.getDefault().getPath("server/", "logPassfile");
        Base64.Encoder encoder = Base64.getEncoder();
        Files.write(p, encoder.encode(auth_key), StandardOpenOption.CREATE);
    }
}
