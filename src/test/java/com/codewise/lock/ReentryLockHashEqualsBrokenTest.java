package com.codewise.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.codewise.lock.runnable.LockThread;
import com.codewise.lock.templates.Template;
import com.codewise.lock.wrappers.ReentryLockWrapper;

@RunWith(Parameterized.class)
public class ReentryLockHashEqualsBrokenTest extends Template {

	@Before
	public void setUp() {
		super.setUp();
		testLocker = new ReentryLockWrapper(statistics, false, false);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				// same hashCode/equals - same lock
				{ "lock1", "lock1", new String("lock1"), new String("lock1") },

				// same hashCode/equals - same lock
				{ 1, 1, new Integer(1), Integer.parseInt("1") },

				// different hashCode / same equals - same lock
				// Proof that for locks not fulfilling HashCode-Equals contract Service breaks
				// Equality contract
				{ new EqualsConstant(), new EqualsConstant(), new EqualsConstant(), new EqualsConstant() } };

		return Arrays.asList(data);
	}

	@Test
	public void sameEqualsDifferentHashCode_test() throws InterruptedException {
		// GIVEN
		callableList.add(new LockThread(testLocker, lock_1));
		callableList.add(new LockThread(testLocker, lock_2));
		callableList.add(new LockThread(testLocker, lock_3));
		callableList.add(new LockThread(testLocker, lock_4));

		// WHEN
		List<Future<Boolean>> invokeAll = executor.invokeAll(callableList);
		executor.shutdown();
		executor.awaitTermination(500, TimeUnit.MILLISECONDS);
		
		// THEN
		assertThat(statistics.stream().filter(dto -> dto.getLock().equals(lock_1)).count()).isEqualTo(4);
		long[] lockTimes = statistics.stream().mapToLong(x -> x.getCurrentTime()).toArray();
		assertThat(lockTimes[lockTimes.length - 1] - lockTimes[0]).isCloseTo(150l, within(20l));
	}
}
