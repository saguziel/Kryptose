package org.kryptose.server;

import java.util.ArrayList;

import org.kryptose.requests.Blob;
import org.kryptose.requests.Log;
import org.kryptose.requests.User;
import org.kryptose.server.DataStore;

public class DataStoreMock implements DataStore {

	@Override
	public boolean userHasBlob(User user) {
		return false;
	}

	@Override
	public WriteResult writeBlob(User user, Blob blob, byte[] oldDigest) {
		return null;
	}

	@Override
	public Blob readBlob(User user) {
		return null;
	}

	@Override
	public ArrayList<Log> readUserLogs(User user, int maxEntries) {
		return null;
	}

	@Override
	public WriteResult writeUserLog(User user, Log log) {
		return null;
	}

	@Override
	public WriteResult writeSystemLog(Log log) {
		return null;
	}

	@Override
	public boolean deleteBlob(User user) {
		// TODO Auto-generated method stub
		return false;
	}

}
