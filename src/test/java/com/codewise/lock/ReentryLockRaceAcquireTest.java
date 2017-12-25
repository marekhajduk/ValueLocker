package com.codewise.lock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.with;
import static org.awaitility.Duration.ONE_MILLISECOND;
import static org.awaitility.Duration.TWO_HUNDRED_MILLISECONDS;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@RunWith(Parameterized.class)
public class ReentryLockRaceAcquireTest {
	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object hashEqualsSupport;
	@Parameter(2)
	public Object lockFairness;

	private Locker locker;
	private ListeningScheduledExecutorService executor;
	private CyclicBarrier barrier = new CyclicBarrier(3);

	private Callable<Boolean> call = () -> {
		barrier.await();
		Mutex lock = locker.lock(lock_1);
		MILLISECONDS.sleep(100);
		lock.release();
		return true;
	};

	@Before
	public void setUp() {
		executor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(10));
		locker = new ReentryLocker((Boolean) lockFairness, (Boolean) hashEqualsSupport);
	}

	@After
	public void tearDown() {
		executor.shutdown();
	}

	@Parameters(name = "{index}-LOCK: [{0}] - HASH/EQUALS Support: [{1}], FAIRNESS: [{2}]")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { new String("lock"), false, true }, { new String("lock"), true, true },
				{ new String("lock"), false, false }, { new String("lock"), true, false }, };
		return Arrays.asList(data);
	}

	@Test(timeout = 1000)
	public void fairnessLock_test() throws InterruptedException {
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
