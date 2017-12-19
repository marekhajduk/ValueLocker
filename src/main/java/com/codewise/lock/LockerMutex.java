package com.codewise.lock;

import java.util.concurrent.locks.Lock;

public class LockerMutex implements Mutex{
	private final Object key;
	private final Lock lock;
	
	public LockerMutex(Object key, Lock lock) {
		super();
		this.key = key;
		this.lock = lock;
	}
	
	@Override
	public void release() {
		lock.unlock();
	}
}
