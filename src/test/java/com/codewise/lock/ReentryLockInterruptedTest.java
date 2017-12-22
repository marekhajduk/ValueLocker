package com.codewise.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.codewise.lock.templates.Template;
import com.codewise.lock.wrappers.ReentryLockWrapper;
import com.google.common.util.concurrent.ListenableFuture;

public class ReentryLockInterruptedTest extends Template {
	private boolean interrupted;
	private AtomicInteger atomic;

	@Before
	public void setUp() {
		super.setUp();
		interrupted = true;
		testLocker = new ReentryLockWrapper(statistics, false, true);
		lock_1 = new Object();
		atomic = new AtomicInteger();
	}

	@Test
	public void interruptedLocks_test() throws InterruptedException, ExecutionException {
		// GIVEN
		Callable call = () -> {
			Mutex lock = null;
			try {
				lock = (true == interrupted) ? testLocker.lockInterruptibly(lock_1) : testLocker.lock(lock_1);
			} catch (InterruptedException e1) {
				atomic.incrementAndGet();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			lock.release();
			return 0;
		};

		// WHEN
		ListenableFuture<?> future = executor.submit(call);
		Thread.sleep(10);
		ListenableFuture<?> future2 = executor.submit(call);
		future2.cancel(true);

		executor.shutdown();
		executor.awaitTermination(1500, TimeUnit.MILLISECONDS);
		
		// THEN
		Assertions.assertThat(atomic.get()).isEqualTo(1);
	}

}
