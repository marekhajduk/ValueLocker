package com.codewise.lock;

public interface Mutex {
	boolean executable();
	void release();
}
