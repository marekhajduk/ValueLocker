package com.codewise.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized.Parameter;

import com.codewise.lock.wrappers.dto.StatDto;

public class TemplateTest {
	protected static final int FOUR_THREADS = 4;
	protected List<StatDto> statistics;
	protected Locker testLocker;
	protected ExecutorService executor;
	protected List<Runnable> runnableList;

	@Parameter(0)
	public Object lock_1;
	@Parameter(1)
	public Object lock_2;
	@Parameter(2)
	public Object lock_3;
	@Parameter(3)
	public Object lock_4;

	@Before
	public void setUp() {
		statistics = new CopyOnWriteArrayList();
		runnableList = new ArrayList();
		executor = Executors.newFixedThreadPool(FOUR_THREADS);
	}

	@After
	public void tearDown() {
	}

	protected final static class FullContract {
	}

	protected final static class HashConstant {
		@Override
		public int hashCode() {
			return 1;
		}
	}

	protected final static class EqualsConstant {
		@Override
		public boolean equals(Object arg0) {
			return true;
		}
	}
}
