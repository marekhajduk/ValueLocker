package com.codewise.lock.wrappers.dto;

import lombok.Builder;

@Builder
public class StatDto {
	private final String threadName;
	private final long currentTime;
	private final Object lock;
	
	public String getThreadName() {
		return threadName;
	}

	public long getCurrentTime() {
		return currentTime;
	}

	public Object getLock() {
		return lock;
	}
}
