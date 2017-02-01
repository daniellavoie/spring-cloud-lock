package org.springframework.cloud.lock.client;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.lock.commons.Lock;

public abstract class AbstractLockClient implements LockClient {
	protected Log logger = LogFactory.getLog(getClass());

	private String applicationName;

	private Map<String, String> ownedLocks = new ConcurrentHashMap<>();

	public AbstractLockClient(String applicationName) {
		this.applicationName = applicationName;
	}

	public void executeIfOwned(Runnable runnable) {
		executeIfOwned(applicationName, runnable);
	}

	public void executeIfOwned(String lockKey, Runnable runnable) {
		try {
			Lock lock = lookupLock(lockKey);
			if (isLockOwned(lock))
				runnable.run();
		}
		catch (Exception ex) {
			// TODO - Proper error logging based on exception type.
			logger.error("Error while refreshing lock " + lockKey + ". Message : "
					+ ex.getMessage());
		}
	}

	private synchronized Lock lookupLock(String lockKey) {
		Iterator<Lock> iterator = locks().iterator();
		while (iterator.hasNext()) {
			Lock lock = iterator.next();

			if (lock.getName().equals(lockKey)) {
				if (lock.getExpires().getTime() - new Date().getTime() < 0)
					releaseAndDestoreLock(lock);
				else if (isLockOwned(lock))
					return refreshAndStoreLock(lock);
				else
					return lock;
			}
		}

		return createAndStoreLock(lockKey);
	}

	private Lock createAndStoreLock(String lockKey) {
		Lock lock = create(lockKey);

		ownedLocks.put(lock.getName(), lock.getValue());

		return lock;
	}

	private boolean isLockOwned(Lock lock) {
		String ownedLockValue = ownedLocks.get(lock.getName());

		return ownedLockValue != null && ownedLockValue.equals(lock.getValue());
	}

	private Lock refreshAndStoreLock(Lock lock) {
		Lock refreshedLock = refresh(lock.getName(), lock.getValue());

		ownedLocks.put(lock.getName(), lock.getValue());

		return refreshedLock;
	}

	private void releaseAndDestoreLock(Lock lock) {
		ownedLocks.remove(lock.getName());

		try {
			release(lock.getName(), lock.getValue());
		}
		catch (Exception ex) {
			// TODO - Proper error logging based on exception type.
			logger.error("Error while release lock " + lock.getName() + ". Message : "
					+ ex.getMessage());
		}
	}
}
