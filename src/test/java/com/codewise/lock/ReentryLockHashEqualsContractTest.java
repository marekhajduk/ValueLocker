package com.codewise.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.codewise.lock.runnable.LockOnceThread;
import com.codewise.lock.wrappers.ReentryLockWrapper;
import com.codewise.lock.wrappers.dto.StatDto;

@RunWith(Parameterized.class)
public class ReentryLockHashEqualsContractTest {
	private static final int FOUR_THREADS = 4;
	private HashMap<Long, StatDto> statistics;
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
		Object[][] data = new Object[][] { { "lock", "lock", "lock", "lock" }, { 1, 1, 1, 1 },
				{ new FullContract(), new FullContract(), new FullContract(), new FullContract() },
				{ new HashConstant(), new HashConstant(), new HashConstant(), new HashConstant() },
				{ new EqualsConstant(), new EqualsConstant(), new EqualsConstant(), new EqualsConstant() } };

		return Arrays.asList(data);
	}

	@Before
	public void setUp() {
		statistics = new HashMap();
		testLocker = new ReentryLockWrapper(statistics);
		runnableList = new ArrayList();
		executor = Executors.newFixedThreadPool(FOUR_THREADS);
	}
	
	@After
	public void tearDown() { }

	@Test
	public void test() throws InterruptedException {
		//GIVEN
		runnableList.add(new LockOnceThread(testLocker, lock_1, statistics));
		runnableList.add(new LockOnceThread(testLocker, lock_2, statistics));
		runnableList.add(new LockOnceThread(testLocker, lock_3, statistics));
		runnableList.add(new LockOnceThread(testLocker, lock_4, statistics));
		
		//WHEN
		runnableList.stream().forEach(x-> executor.execute(x));
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
		//THEN 
		
		System.out.println(statistics);
		
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
