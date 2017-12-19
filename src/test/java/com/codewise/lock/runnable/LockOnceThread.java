package com.codewise.lock.runnable;

import java.util.HashMap;
import java.util.Map;

import org.jooq.lambda.Unchecked;

import com.codewise.lock.Locker;
import com.codewise.lock.Mutex;
import com.codewise.lock.wrappers.dto.ACTION;
import com.codewise.lock.wrappers.dto.StatDto;

public class LockOnceThread implements Runnable {

	private final Locker locker;
	
	private final Object lockValue;
	
	private final Map<Long, StatDto> statistics;
	
	public LockOnceThread(Locker locker, Object lockValue, HashMap<Long, StatDto> statistics) {
		super();
		this.locker = locker;
		this.lockValue = lockValue;
		this.statistics = statistics;
	}
	
	@Override
	public void run() {
		
		Mutex lock = locker.lock(lockValue);
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			Unchecked.throwChecked(e);
		}
		
		statistics.put(
				System.nanoTime(), 
				StatDto.builder().action(ACTION.UNLOCK)
				.threadName(Thread.currentThread().getName())
				.build());
		
		lock.release();
	}
}
