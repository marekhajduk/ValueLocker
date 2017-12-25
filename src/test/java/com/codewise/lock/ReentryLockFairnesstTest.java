package com.codewise.lock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@RunWith(Parameterized.class)
public class ReentryLockFairnesstTest {
	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object hashEqualsSupport;
	@Parameter(2)
	public Object lockFairness;

	private Locker locker;
	private ListeningScheduledExecutorService executor;
	private AtomicInteger atomicValue = new AtomicInteger();

	private Callable<Boolean> call = () -> {
		Mutex lock = null;
		lock = locker.lock(lock_1);
		MILLISECONDS.sleep(50);
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
		Object[][] data = new Object[][] { { new String("lock"), false, true }, { new String("lock"), true, true} };
		return Arrays.asList(data);
	}

	@Test(timeout = 10000)
	public void fairnessLock_test() throws InterruptedException {
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		ListenableScheduledFuture<Boolean> future2 = executor.schedule(call, 5, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future3 = executor.schedule(call, 10, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future4 = executor.schedule(call, 15, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future5 = executor.schedule(call, 20, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future6 = executor.schedule(call, 25, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future7 = executor.schedule(call, 30, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future8 = executor.schedule(call, 35, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future9 = executor.schedule(call, 40, TimeUnit.MILLISECONDS);
		ListenableScheduledFuture<Boolean> future10 = executor.schedule(call, 45, TimeUnit.MILLISECONDS);

		// THEN
		Futures.addCallback(future, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(0, 1);
			}
		});

		Futures.addCallback(future2, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(1, 2);
			}
		});

		Futures.addCallback(future3, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(2, 3);
			}
		});

		Futures.addCallback(future4, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(3, 4);
			}
		});

		Futures.addCallback(future5, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(4, 5);
			}
		});

		Futures.addCallback(future6, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(5, 6);
			}
		});
		
		Futures.addCallback(future7, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(6, 7);
			}
		});
		
		Futures.addCallback(future8, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(7, 8);
			}
		});
		
		Futures.addCallback(future9, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(8, 9);
			}
		});
		
		Futures.addCallback(future10, new TestFutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				atomicValue.compareAndExchange(9, 10);
			}
		});

		Awaitility.await().until(() -> future10.isDone());
		Assertions.assertThat(atomicValue.get()).isEqualTo(10);
	}

	private static interface TestFutureCallback<Boolean> extends FutureCallback<Boolean> {
		public default void onFailure(Throwable t) {
			fail("Future failure : " + t.getMessage());
		}
	}
}