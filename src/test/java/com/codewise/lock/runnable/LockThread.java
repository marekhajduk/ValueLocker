package com.codewise.lock.runnable;

import org.jooq.lambda.Unchecked;

import com.codewise.lock.Locker;
import com.codewise.lock.Mutex;

public class LockThread implements Runnable {
	private final Locker locker;
	private final Object lockValue;

	public LockThread(Locker locker, Object lockValue) {
		super();
		this.locker = locker;
		this.lockValue = lockValue;
	}

	@Override
	public void run() {

		Mutex lock = locker.lock(lockValue);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Unchecked.throwChecked(e);
		}

		lock.release();
	}
}
