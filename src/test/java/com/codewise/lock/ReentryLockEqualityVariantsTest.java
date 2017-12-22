package com.codewise.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.codewise.lock.runnable.LockThread;
import com.codewise.lock.templates.Template;
import com.codewise.lock.wrappers.ReentryLockWrapper;

@RunWith(Parameterized.class)
public class ReentryLockEqualityVariantsTest extends Template {

	@Before
	public void setUp() {
		super.setUp();
		testLocker = new ReentryLockWrapper(statistics, false, true);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				// same hashCode/equals - same lock
				{ "lock1", "lock1", new String("lock1"), new String("lock1") },

				// same hashCode/equals - same lock
				{ 1, 1, Integer.parseInt("1"), Integer.parseInt("1")},
				};

		return Arrays.asList(data);
	}

	@Test
	public void interruptedLocks_test() throws InterruptedException {
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
		Assertions.assertThat(statistics.stream().filter(dto -> dto.getLock().equals(lock_1)).count()).isEqualTo(4);
		long[] aquireTimes = statistics.stream().mapToLong(x -> x.getCurrentTime()).toArray();
		assertThat(aquireTimes[aquireTimes.length - 1] - aquireTimes[0]).isCloseTo(150l, within(30l));
	}
}
