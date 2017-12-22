package com.codewise.lock.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized.Parameter;

import com.codewise.lock.Locker;
import com.codewise.lock.wrappers.dto.StatDto;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import lombok.NoArgsConstructor;

public class Template {
	protected static final int FOUR_THREADS = 4;
	protected List<StatDto> statistics;
	protected Locker testLocker;
	protected ListeningExecutorService  executor;
	protected List<Callable<Boolean>> callableList;

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
		callableList = new ArrayList();
		executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(FOUR_THREADS));
	}

	@After
	public void tearDown() {
	}
	
	@NoArgsConstructor
	protected final static class FullContract {
	}

	@NoArgsConstructor
	protected final static class HashConstant {
		@Override
		public int hashCode() {
			return 1;
		}
	}

	@NoArgsConstructor
	protected final static class EqualsConstant {
		@Override
		public boolean equals(Object arg0) {
			return true;
		}
	}
}
