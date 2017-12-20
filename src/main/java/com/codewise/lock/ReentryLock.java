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
		this.lockers = new ConcurrentWeakValueMap<Object, ReentrantLock>(equalsHashContract);
	}

	
//	public Mutex lockInterruptibly(Object key) {
//		
//	}
//	
//	public Mutex lock() {
//		
//	}
	
	public Mutex lock(Object key) {
		Lock lock= lockers.compute(key, (k, value) -> {
			if (null == value) {
				return new ReentrantLock();
			} else {
				return value;
			}});
			
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e) {
				Unchecked.throwChecked(e);
			}
		return new LockerMutex(key, lock);
	}

}
