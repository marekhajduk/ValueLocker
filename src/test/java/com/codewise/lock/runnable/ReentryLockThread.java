package com.codewise.lock.runnable;

import java.util.concurrent.Callable;

import org.jooq.lambda.Unchecked;

import com.codewise.lock.Locker;
import com.codewise.lock.Mutex;

public class ReentryLockThread implements Callable<Boolean> {
	private final Locker locker;
	private final Object lockValue;

	public ReentryLockThread(Locker locker, Object lockValue) {
		super();
		this.locker = locker;
		this.lockValue = lockValue;
	}

	@Override
	public Boolean call() {
		Mutex lock = locker.lock(lockValue);
		lock = locker.lock(lockValue);

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Unchecked.throwChecked(e);
		}
		lock.release();
		return true;
	}
}
