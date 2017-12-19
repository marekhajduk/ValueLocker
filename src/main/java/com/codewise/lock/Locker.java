package com.codewise.lock;

public interface Locker {
	Mutex lock(Object obj);
}
