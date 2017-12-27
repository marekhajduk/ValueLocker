package com.codewise.lock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.codewise.lock.exceptions.NonAcquiredLockException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@RunWith(Parameterized.class)
public class ReentryLockTryLockTest {
	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object hashEqualsSupport;
	@Parameter(2)
	public Object lockFairness;

	private Locker locker;
	private ListeningExecutorService executor;
	private CountDownLatch latch = new CountDownLatch(1);

	private Callable<Boolean> call = () -> {
		Mutex lock = locker.lock(lock_1);
		latch.countDown();
		MILLISECONDS.sleep(50);
		lock.release();
		return true;
	};

	@Before
	public void setUp() {
		executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
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

	@Test(timeout = 500)
	public void tryLock_test() throws InterruptedException {
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		latch.await();
		Mutex mutex1 = locker.tryLock(lock_1);
		Awaitility.await().until(() -> future.isDone());
		Mutex mutex2 = locker.tryLock(lock_1);
		mutex2.release();

		// THEN
		Assertions.assertThat(mutex1.executable()).isEqualTo(false);
		Assertions.assertThat(mutex2.executable()).isEqualTo(true);
	}

	@Test(timeout = 500, expected = NonAcquiredLockException.class)
	public void tryLockException_test() throws InterruptedException {
		// WHEN
		executor.submit(call);
		latch.await();
		Mutex mutex1 = locker.tryLock(lock_1);
		mutex1.release();
	}

	@Test(timeout = 500)
	public void tryLockWithTimeout_test() throws InterruptedException {
		// WHEN
		executor.submit(call);
		latch.await();

		Mutex mutex1 = locker.tryLock(lock_1, 100, MILLISECONDS);
		mutex1.release();

		// THEN
		Assertions.assertThat(mutex1.executable()).isEqualTo(true);
	}

	@Test(timeout = 500, expected = NonAcquiredLockException.class)
	public void tryLockWithTimeoutException_test() throws InterruptedException {
		// WHEN
		executor.submit(call);
		latch.await();
		Mutex mutex1 = locker.tryLock(lock_1, 20, MILLISECONDS);
		mutex1.release();
	}
}
