package com.codewise.lock.runnable;

import java.util.concurrent.Callable;

import org.jooq.lambda.Unchecked;

import com.codewise.lock.Locker;
import com.codewise.lock.Mutex;

public class LockThread implements Callable<Boolean> {
	private final Locker locker;
	private final Object lockValue;

	public LockThread(Locker locker, Object lockValue) {
		super();
		this.locker = locker;
		this.lockValue = lockValue;
	}

	@Override
	public Boolean call() {
		Mutex lock = null;
		lock = locker.lock(lockValue);

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}

		lock.release();
		return true;
	}
}
