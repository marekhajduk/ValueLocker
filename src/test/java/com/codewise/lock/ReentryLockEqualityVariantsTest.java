package com.codewise.lock;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.codewise.lock.runnable.LockThread;
import com.codewise.lock.runnable.ReentryLockThread;
import com.codewise.lock.wrappers.ReentryLockWrapper;

@RunWith(Parameterized.class)
public class ReentryLockEqualityVariantsTest extends TemplateTest {

	@Before
	public void setUp() {
		super.setUp();
		testLocker = new ReentryLockWrapper(statistics, true);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				// different hashCode/equals - independent locks
//				{ "lock1", "lock1", new String("lock1"), new String("lock1") },

				// different hashCode/equals - independent locks
//				{ 1, 2, 3, 4 },

				// different hashCode/equals - independent locks
//				{ new FullContract(), new FullContract(), new FullContract(), new FullContract() },

				// same hashCode/ different equals - independent locks
//				{ new HashConstant(), new HashConstant(), new HashConstant(), new HashConstant() },

				// different hashCode / same equals - independent locks
				// Proof that for locks not fulfilling HashCode-Equals contract Service breaks
				// Equality contract
				{ new EqualsConstant(), new EqualsConstant(), new EqualsConstant(), new EqualsConstant() } };

		return Arrays.asList(data);
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

	@Ignore
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
}
