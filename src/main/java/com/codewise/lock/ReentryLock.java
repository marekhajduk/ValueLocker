package com.codewise.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jooq.lambda.Unchecked;

public class ReentryLock implements Locker {
	private final ConcurrentWeakValueMap<Object, ReentrantLock> lockers;

	public ReentryLock() {
		this(true);
	}
	
	public ReentryLock(boolean equalsHashContract) {
		this.lockers  = new ConcurrentWeakValueMap<Object, ReentrantLock>(equalsHashContract);
	}
	
	public Mutex lock(Object key) {
		Lock lock= lockers.compute(key, (k, value) -> {
			if (null == value) {
				ReentrantLock s = new ReentrantLock();
				try {
					s.lockInterruptibly();
				} catch (InterruptedException e) {
					Unchecked.throwChecked(e);
				}
				return s;
			} else {
				try {
					value.lockInterruptibly();
				} catch (InterruptedException e) {
					Unchecked.throwChecked(e);
				}
				return value;
			}
		});
		
		return new LockerMutex(key, lock);
	}

}
