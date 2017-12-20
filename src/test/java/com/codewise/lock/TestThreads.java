package com.codewise.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class TestThreads {
	public static void main(String[] args) {
		
	String str = new String("dla");
	String str2 = new String("dla");
	String str3 = new String("dla");
	Integer i = new Integer(200);
	Integer i2 = new Integer(200);
	
		
		System.out.println(i.hashCode());
		System.out.println(i2.hashCode());
		System.out.println(str3.hashCode());
		
	}
}

class TestTh1 implements Runnable {
	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName());
		
	}
}

