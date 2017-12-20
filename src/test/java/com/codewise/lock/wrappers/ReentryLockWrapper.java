package com.codewise.lock.wrappers;

import java.util.List;

import javax.inject.Inject;

import com.codewise.lock.Mutex;
import com.codewise.lock.ReentryLock;
import com.codewise.lock.wrappers.dto.StatDto;

public class ReentryLockWrapper extends ReentryLock {
	@Inject
	private final List<StatDto> statistics;
	
	public ReentryLockWrapper(List<StatDto> statistics, boolean hashEqualsContract) {
		super(hashEqualsContract);
		this.statistics = statistics;
	}

	@Override
	public Mutex lock(Object key) {
		Mutex mutex = super.lock(key);
		
		statistics.add(
				StatDto.builder().threadName(Thread.currentThread().getName())
				.currentTime(System.currentTimeMillis()).lock(key).build());
		
		return mutex;
	}
}
