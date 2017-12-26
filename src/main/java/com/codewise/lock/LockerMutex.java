package com.codewise.lock;

import java.util.concurrent.locks.Lock;

import com.codewise.lock.exceptions.NonAcquiredLockException;

public class LockerMutex implements Mutex {
	private final Object key;
	private final Lock lock;
	private final boolean runnable;

	public LockerMutex(Object key, Lock lock) {
		this(key, lock, true);
	}

	public LockerMutex(Object key, Lock lock, boolean runnable) {
		super();
		this.key = key;
		this.lock = lock;
		this.runnable = runnable;
	}

	@Override
	public boolean executable() {
		return this.runnable;
	}

	@Override
	public void release() {
		if (runnable) {
			lock.unlock();
		} else {
			throw new NonAcquiredLockException();
		}
	}
}
