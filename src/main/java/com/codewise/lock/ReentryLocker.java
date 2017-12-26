package com.codewise.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentryLocker implements Locker {
	private final ConcurrentWeakValueMap<Object, ReentrantLock> lockers;
	private boolean fair;

	public ReentryLocker() {
		this(false,true);
	}
	public ReentryLocker(boolean fair) {
		this(fair, true);
	}
	
	public ReentryLocker(boolean fair, boolean equalsHashContract) {
		this.fair = fair;
		this.lockers = new ConcurrentWeakValueMap<>(equalsHashContract);
	}

	@Override
	public Mutex lockInterruptibly(Object key) throws InterruptedException {
		Lock lock = lockValue(key);
		lock.lockInterruptibly();
		return new LockerMutex(key, lock);
	}

	@Override
	public Mutex lock(Object key) {
		Lock lock = lockValue(key);
		lock.lock();
		return new LockerMutex(key, lock);
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

	@Override
	public Mutex tryLock(Object key) {
		Lock lock = lockValue(key);
		boolean taken = lock.tryLock();
		return new LockerMutex(key, lock, taken);
	}
	
	@Override
	public Mutex tryLock(Object key, long timeout, TimeUnit unit) throws InterruptedException {
		Lock lock = lockValue(key);
		boolean taken = lock.tryLock(timeout, unit);
		return new LockerMutex(key, lock, taken);
	}	
	
	public int lockerSize() {
		return lockers.size();
	}
}
