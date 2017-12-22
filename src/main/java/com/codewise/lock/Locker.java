package com.codewise.lock;

import java.util.concurrent.TimeUnit;

public interface Locker {
	Mutex lock(Object obj);
	Mutex lockInterruptibly(Object obj) throws InterruptedException;
	Mutex tryLock();
	Mutex tryLock(long timeout, TimeUnit unit);
	
}
