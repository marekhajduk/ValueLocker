package com.codewise.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentryLock implements Locker {
	private final ConcurrentWeakValueMap<Object, ReentrantLock> lockers;
	private boolean fair;

	public ReentryLock() {
		this(false,true);
	}
	public ReentryLock(boolean fair) {
		this(true, true);
	}
	
	public ReentryLock(boolean fair, boolean equalsHashContract) {
		this.fair = fair;
		this.lockers = new ConcurrentWeakValueMap<>(equalsHashContract);
	}

	@Override
	public Mutex lockInterruptibly(Object key) throws InterruptedException {
		Lock lock = lockValue(key);
		reservedInterruptibly(lock);
		return new LockMutex(key, lock);
	}

	@Override
	public Mutex lock(Object key) {
		Lock lock = lockValue(key);
		reserved(lock);
		return new LockMutex(key, lock);
	}

	private Lock lockValue(Object key) {
		return lockers.compute(key, (k, value) -> {
			if (null == value) {
				return new ReentrantLock(fair);
			} else {
				return value;
			}
		});
	}

	private void reserved(Lock lock) {
		lock.lock();
	}

	private void reservedInterruptibly(Lock lock) throws InterruptedException  {
			lock.lockInterruptibly();
	}
	
	@Override
	public Mutex tryLock() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Mutex tryLock(long timeout, TimeUnit unit) {
		throw new UnsupportedOperationException();
	}
}
