package io.revealbi.sdk.ext.oauth;

import java.util.HashMap;
import java.util.Map;

public class TokenLock {
	private final Map<String, Lock> locks = new HashMap<String, Lock>();

	public Lock getLockObject(String cacheKey) {
		Lock lock;
		synchronized (locks) {
			lock = locks.get(cacheKey);
			if (lock == null) {
				lock = new Lock();
				locks.put(cacheKey, lock);
			}
			lock.references++;
		}
		return lock;
	}
	
	public void releaseLock(String cacheKey, Lock lock) {
		synchronized (locks) {
			lock.references--;
			if (lock.references == 0) {
				locks.remove(cacheKey);
			}
		}
	}
	
	public static class Lock {
		int references;
	}
}
