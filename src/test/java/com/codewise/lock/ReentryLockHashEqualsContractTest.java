package com.codewise.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.codewise.lock.runnable.LockThread;
import com.codewise.lock.runnable.ReentryLockThread;
import com.codewise.lock.wrappers.ReentryLockWrapper;
import com.codewise.lock.wrappers.dto.StatDto;

@RunWith(Parameterized.class)
public class ReentryLockHashEqualsContractTest {
	private static final int FOUR_THREADS = 4;
	private List<StatDto> statistics;
	private Locker testLocker;
	private ExecutorService executor;
	private List<Runnable> runnableList;

	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object lock_2;
	@Parameter(2)
	public Object lock_3;
	@Parameter(3)
	public Object lock_4;

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				// different hashCode/equals - independent locks
				{ "lock1", "lock2", "lock3", "lock4" },

				// different hashCode/equals - independent locks
				{ 1, 2, 3, 4 },

				// different hashCode/equals - independent locks
				{ new FullContract(), new FullContract(), new FullContract(), new FullContract() },

				// same hashCode/ different equals - independent locks
				{ new HashConstant(), new HashConstant(), new HashConstant(), new HashConstant() },

				// different hashCode / same equals - independent locks 
				// Proof that for locks not fulfilling HashCode-Equals contract Service breaks Equality contract
				{ new EqualsConstant(), new EqualsConstant(), new EqualsConstant(), new EqualsConstant() } };

		return Arrays.asList(data);
	}

	@Before
	public void setUp() {
		statistics = new CopyOnWriteArrayList();
		testLocker = new ReentryLockWrapper(statistics, true);
		runnableList = new ArrayList();
		executor = Executors.newFixedThreadPool(FOUR_THREADS);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void independentLocks_test() throws InterruptedException {
		// GIVEN
		runnableList.add(new LockThread(testLocker, lock_1));
		runnableList.add(new LockThread(testLocker, lock_2));
		runnableList.add(new LockThread(testLocker, lock_3));
		runnableList.add(new LockThread(testLocker, lock_4));

		// WHEN
		runnableList.stream().forEach(x -> executor.execute(x));
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);

		// THEN
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_1).count()).isEqualTo(1);
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_2).count()).isEqualTo(1);
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_3).count()).isEqualTo(1);
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_4).count()).isEqualTo(1);
	}

	@Test
	public void reentryLocks_test() throws InterruptedException {
		// GIVEN
		runnableList.add(new ReentryLockThread(testLocker, lock_1));
		runnableList.add(new ReentryLockThread(testLocker, lock_2));
		runnableList.add(new ReentryLockThread(testLocker, lock_3));
		runnableList.add(new ReentryLockThread(testLocker, lock_4));

		// WHEN
		runnableList.stream().forEach(x -> executor.execute(x));
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);

		// THEN
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_1).count()).isEqualTo(2);
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_2).count()).isEqualTo(2);
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_3).count()).isEqualTo(2);
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock() == lock_4).count()).isEqualTo(2);

	}
	
	private final static class FullContract {
	}

	private final static class HashConstant {
		@Override
		public int hashCode() {
			return 1;
		}
	}

	private final static class EqualsConstant {
		@Override
		public boolean equals(Object arg0) {
			return true;
		}
	}
}
