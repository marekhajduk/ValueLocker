package com.codewise.lock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.with;
import static org.awaitility.Duration.ONE_MILLISECOND;
import static org.awaitility.Duration.TWO_HUNDRED_MILLISECONDS;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@RunWith(Parameterized.class)
public class ReentryLockEqualityVariantsTest {
	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object lock_2;
	@Parameter(2)
	public Object lock_3;
	@Parameter(3)
	public Object hashEqualsSupport;
	@Parameter(4)
	public Object lockFairness;

	private Locker locker;
	private AtomicInteger lockReference;
	private ListeningExecutorService executor;

	@Before
	public void setUp() {
		executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
		lockReference = new AtomicInteger();
		locker = new ReentryLocker((Boolean)lockFairness, (Boolean)hashEqualsSupport);
	}

	@After
	public void tearDown() {
		executor.shutdown();
	}
	private Supplier<Object> sup = () -> {
		switch (lockReference.incrementAndGet()) {
		case 1:
			return lock_1;
		case 2:
			return lock_2;
		case 3:
			return lock_3;
		default:
			throw new UnsupportedOperationException();
		}
	};

	private Callable<Boolean> call = () -> {
		Mutex lock = null;
		lock = locker.lock(sup.get());
		MILLISECONDS.sleep(100);
		lock.release();
		return true;
	};

	@Parameters(name = "{index}-LOCKS: [{0}, {1}, {2}] - HASH/EQUALS Support: {3}, FAIRNESS: {4}")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { new String("lock"), "lock", new String("lock"), false, false },
				{ new String("lock"), "lock", new String("lock"), true, false },
				{ new String("lock"), "lock", new String("lock"), false, true },
				{ new String("lock"), "lock", new String("lock"), true, true } };
		return Arrays.asList(data);
	}

	@Test(timeout = 500)
	public void equallyLocks_test() throws InterruptedException {
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		ListenableFuture<Boolean> future2 = executor.submit(call);
		ListenableFuture<Boolean> future3 = executor.submit(call);

		// THEN
		with().pollDelay(TWO_HUNDRED_MILLISECONDS).and().with().pollInterval(ONE_MILLISECOND).await()
				.atLeast(250, MILLISECONDS).atMost(350, MILLISECONDS)
				.until(() -> future.isDone() && future2.isDone() && future3.isDone());

	}
}
