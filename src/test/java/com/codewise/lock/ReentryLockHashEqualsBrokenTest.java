package com.codewise.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.codewise.lock.runnable.LockThread;
import com.codewise.lock.wrappers.ReentryLockWrapper;

@RunWith(Parameterized.class)
public class ReentryLockHashEqualsBrokenTest extends TemplateTest {

	@Before
	public void setUp() {
		super.setUp();
		testLocker = new ReentryLockWrapper(statistics, false);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				// same hashCode/equals - independent locks
				{ "lock1", "lock1", "lock1", "lock1" },

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
		executor.awaitTermination(5, TimeUnit.SECONDS);

		// THEN
		assertThat(statistics.stream().filter(dto -> dto.getLock().equals(lock_1)).count()).isEqualTo(4);
		long[] lockTimes = statistics.stream().mapToLong(x -> x.getCurrentTime()).toArray();
		assertThat(lockTimes[lockTimes.length - 1] - lockTimes[0]).isCloseTo(150l, within(20l));
	}
}
