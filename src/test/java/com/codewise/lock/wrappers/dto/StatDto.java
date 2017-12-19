package com.codewise.lock.wrappers.dto;

import lombok.Builder;

@Builder
public class StatDto {
	private final String threadName;
	private final ACTION action;
	
	public String getThreadName() {
		return threadName;
	}

	public ACTION getAction() {
		return action;
	}

	public StatDto(String threadName, ACTION action) {
		this.threadName = threadName;
		this.action = action;
	}
}
