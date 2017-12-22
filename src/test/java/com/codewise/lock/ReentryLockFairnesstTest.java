package com.codewise.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.codewise.lock.templates.Template;
import com.codewise.lock.wrappers.ReentryLockWrapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class ReentryLockFairnesstTest extends Template {
	private Callable<Boolean> call;
	private long complete_1;
	private long complete_2;
	private long complete_3;
	
	
	@Before
	public void setUp() {
		super.setUp();
		lock_1 = new Object();
		testLocker = new ReentryLockWrapper(statistics, true, true);

		call = () -> {
			Mutex lock = null;
			lock = testLocker.lock(lock_1);

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}

			lock.release();
			return true;
		};
	}

	@Test
	public void fairness_test() throws InterruptedException {
		// WHEN
		ListenableFuture<Boolean> future = executor.submit(call);
		Thread.sleep(10);
		ListenableFuture<Boolean> future2 = executor.submit(call);
		Thread.sleep(10);
		ListenableFuture<Boolean> future3 = executor.submit(call);
		
		Futures.addCallback(future, new FutureCallback<Boolean>() {
		    @Override
		    public void onSuccess(Boolean contents) {
		    	complete_1 = System.currentTimeMillis();
		    }

		    @Override
		    public void onFailure(Throwable throwable) {
		    }
		});
		
		Futures.addCallback(future2, new FutureCallback<Boolean>() {
		    @Override
		    public void onSuccess(Boolean contents) {
		    	complete_2 = System.currentTimeMillis();
		    }

		    @Override
		    public void onFailure(Throwable throwable) {
		    }
		});
		Futures.addCallback(future3 , new FutureCallback<Boolean>() {
		    @Override
		    public void onSuccess(Boolean contents) {
		    	complete_3 = System.currentTimeMillis();
		    }

		    @Override
		    public void onFailure(Throwable throwable) {
		    }
		});
		
		executor.shutdown();
		executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
		
		// THEN
		Assertions.assertThat(complete_1).isLessThan(complete_2).isLessThan(complete_3);
	}
}
