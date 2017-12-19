package com.codewise.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class TestThreads {
	public static void main(String[] args) {
		
		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("ORDERS-%d")
				.build();
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		executorService.execute(new TestTh1());
		
		
		
	}
}

class TestTh1 implements Runnable {
	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName());
		
	}
}

