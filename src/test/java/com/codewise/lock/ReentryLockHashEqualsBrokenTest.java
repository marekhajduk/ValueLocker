package com.codewise.lock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.with;
import static org.awaitility.Duration.ONE_MILLISECOND;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.awaitility.Duration;
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

import lombok.NoArgsConstructor;

@RunWith(Parameterized.class)
public class ReentryLockHashEqualsBrokenTest {
	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object lock_2;
	@Parameter(2)
	public Object lock_3;

	private Locker locker;
	private AtomicInteger lockReference;
	private ListeningExecutorService executor;

	@Before
	public void setUp() {
		executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
		lockReference = new AtomicInteger();
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

	@Parameters(name = "{index}-LOCKS: [{0}, {1}, {2}]")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				// different hashCode / same equals - same lock
				{new EqualsConstant(), new EqualsConstant(), new EqualsConstant() } };
		return Arrays.asList(data);
	}

	@Test(timeout = 500)
	public void supportFairAndBreakHashCodeContract_test() throws InterruptedException {
		//GIVEN
		locker = new ReentryLocker(true, false);
	
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		ListenableFuture<Boolean> future2 = executor.submit(call);
		ListenableFuture<Boolean> future3 = executor.submit(call);

		// THEN
		with().pollDelay(Duration.TWO_HUNDRED_MILLISECONDS).and().with().pollInterval(ONE_MILLISECOND).await()
				.atLeast(250, MILLISECONDS).atMost(350, MILLISECONDS)
				.until(() -> future.isDone() && future2.isDone() && future3.isDone());
	}

	@Test(timeout = 500)
	public void UnFairAndBreakHashCodeContract_test() throws InterruptedException {
		//GIVEN
		locker = new ReentryLocker(false, false);
		
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		ListenableFuture<Boolean> future2 = executor.submit(call);
		ListenableFuture<Boolean> future3 = executor.submit(call);
		
		// THEN
		with().pollDelay(Duration.TWO_HUNDRED_MILLISECONDS).and().with().pollInterval(ONE_MILLISECOND).await()
		.atLeast(250, MILLISECONDS).atMost(350, MILLISECONDS)
		.until(() -> future.isDone() && future2.isDone() && future3.isDone());
	}
	
	@Test(timeout = 500)
	public void supportFairAndHashCodeContract_test() throws InterruptedException {
		//GIVEN
		locker = new ReentryLocker(true, true);
	
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		ListenableFuture<Boolean> future2 = executor.submit(call);
		ListenableFuture<Boolean> future3 = executor.submit(call);

		// THEN
		with().pollDelay(80, TimeUnit.MILLISECONDS).and().with().pollInterval(ONE_MILLISECOND).await()
				.atLeast(90, MILLISECONDS).atMost(150, MILLISECONDS)
				.until(() -> future.isDone() && future2.isDone() && future3.isDone());
	}	

	@Test(timeout = 500)
	public void UnFairAndHashCodeContract_test() throws InterruptedException {
		//GIVEN
		locker = new ReentryLocker(false, true);
	
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		ListenableFuture<Boolean> future2 = executor.submit(call);
		ListenableFuture<Boolean> future3 = executor.submit(call);

		// THEN
		with().pollDelay(80, TimeUnit.MILLISECONDS).and().with().pollInterval(ONE_MILLISECOND).await()
				.atLeast(90, MILLISECONDS).atMost(150, MILLISECONDS)
				.until(() -> future.isDone() && future2.isDone() && future3.isDone());
	}	
	
	@NoArgsConstructor
	protected final static class EqualsConstant {
		@Override
		public boolean equals(Object arg0) {
			return true;
		}
	}
}
// @Before
// public void setUp() {
// super.setUp();
// testLocker = new ReentryLockWrapper(statistics, false, false);
// }
//
// @Parameters
// public static Collection<Object[]> data() {
// Object[][] data = new Object[][] {
// // same hashCode/equals - same lock
// { "lock1", "lock1", new String("lock1"), new String("lock1") },
//
// // same hashCode/equals - same lock
// { 1, 1, new Integer(1), Integer.parseInt("1") },
//
// // different hashCode / same equals - same lock
// // Proof that for locks not fulfilling HashCode-Equals contract Service
// breaks
// // Equality contract
// { new EqualsConstant(), new EqualsConstant(), new EqualsConstant(), new
// EqualsConstant() } };
//
// return Arrays.asList(data);
// }
//
// @Test
// public void sameEqualsDifferentHashCode_test() throws InterruptedException {
// // GIVEN
// callableList.add(new LockThread(testLocker, lock_1));
// callableList.add(new LockThread(testLocker, lock_2));
// callableList.add(new LockThread(testLocker, lock_3));
// callableList.add(new LockThread(testLocker, lock_4));
//
// // WHEN
// List<Future<Boolean>> invokeAll = executor.invokeAll(callableList);
// executor.shutdown();
// executor.awaitTermination(500, TimeUnit.MILLISECONDS);
//
// // THEN
// assertThat(statistics.stream().filter(dto ->
// dto.getLock().equals(lock_1)).count()).isEqualTo(4);
// long[] lockTimes = statistics.stream().mapToLong(x ->
// x.getCurrentTime()).toArray();
// assertThat(lockTimes[lockTimes.length - 1] - lockTimes[0]).isCloseTo(150l,
// within(20l));
// }
// }
