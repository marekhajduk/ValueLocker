package com.codewise.lock;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReentryLockInterruptedTest {
	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object hashEqualsSupport;
	@Parameter(2)
	public Object lockFairness;

	private Locker locker;
	private ScheduledExecutorService executor;
	private AtomicInteger atomicValue = new AtomicInteger();

	@Before
	public void setUp() {
		executor = Executors.newScheduledThreadPool(3);
		locker = new ReentryLocker((Boolean) lockFairness, (Boolean) hashEqualsSupport);
	}

	@After
	public void tearDown() {
		executor.shutdown();
	}

	@Parameters(name = "{index}-LOCK: [{0}] - HASH/EQUALS Support: [{1}], FAIRNESS: [{2}]")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { new String("lock"), false, true }, { new String("lock"), true, true },
				{ new String("lock"), false, false }, { new String("lock"), true, false } };
		return Arrays.asList(data);
	}

	@Test(timeout = 300)
	public void interruptedLock_test() throws InterruptedException {
		// GIVEN
		Callable<Boolean> call = () -> {
			Mutex lock = null;
			try {
				lock = locker.lockInterruptibly(lock_1);
			} catch (InterruptedException e1) {
				atomicValue.incrementAndGet();
			}
			TimeUnit.MILLISECONDS.sleep(100);

			lock.release();
			return true;
		};
		// WHEN
		Future<Boolean> future = executor.submit(call);
		Future<Boolean> future2 = executor.schedule(call, 5, TimeUnit.MILLISECONDS);
		executor.schedule(() -> future2.cancel(true), 40, TimeUnit.MILLISECONDS);

		// THEN
		Awaitility.await().until(() -> future.isDone() && future2.isDone());
		Assertions.assertThat(atomicValue.get()).isEqualTo(1);
	}

	@Test(timeout = 300)
	public void nonInterruptedLock_test() throws InterruptedException {
		// GIVEN
		Callable<Boolean> call = () -> {
			Mutex lock = null;
			try {
				lock = locker.lock(lock_1);
			} catch (Throwable e1) {
				atomicValue.incrementAndGet();
			}
			TimeUnit.MILLISECONDS.sleep(100);

			lock.release();
			return true;
		};
		// WHEN
		Future<Boolean> future = executor.submit(call);
		Future<Boolean> future2 = executor.schedule(call, 5, TimeUnit.MILLISECONDS);
		executor.schedule(() -> future2.cancel(true), 40, TimeUnit.MILLISECONDS);

		// THEN
		Awaitility.await().until(() -> future.isDone() && future2.isDone());
		Assertions.assertThat(atomicValue.get()).isEqualTo(0);
	}
}
