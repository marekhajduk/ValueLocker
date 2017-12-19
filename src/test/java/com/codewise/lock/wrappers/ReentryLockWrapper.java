package com.codewise.lock.wrappers;

import java.util.HashMap;

import javax.inject.Inject;

import com.codewise.lock.Mutex;
import com.codewise.lock.ReentryLock;
import com.codewise.lock.wrappers.dto.ACTION;
import com.codewise.lock.wrappers.dto.StatDto;

public class ReentryLockWrapper extends ReentryLock {
	@Inject
	private final HashMap<Long, StatDto> statistics ;
	
	public ReentryLockWrapper(HashMap<Long, StatDto> statistics) {
		this.statistics = statistics;
	}

	@Override
	public Mutex lock(Object key) {
		Mutex mutex = super.lock(key);
		
		statistics.put(
				System.nanoTime(), 
				StatDto.builder().action(ACTION.LOCK)
				.threadName(Thread.currentThread().getName())
				.build());
		
		return mutex;
	}
}
