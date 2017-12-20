# ValueLocker
Java lock service that is able to synchronize on object's value
Object's value is identified by object's equals() implementation.

```java
public interface Locker {
   Mutex lock(Object obj);
}

public interface Mutex {
   void release();
}
```
following code run from different threads should be mutual exclusive.

```java
Locker valueLocker = new MyLockerImpl(); 

thread A: valueLocker.lock(new String("A"));
thread B: valueLocker.lock("A");
```

# Principles #

Lock service background structure is combination of WeakReferenceQueue and ConcurrentHashMap
 * providing weak keys
 * non-blocking access
 
 Computational complexity - access to Lock element
 
| Lock object  			| O(n)  |
| ---------------------	| -----:|
| Hash/Equals contract  |  O(1) |
| Comparable (H/E break)|O(logn)|
| Other cases    		|  O(n) |


Lock implementation supported:


| Lock implementation	|       |
| ---------------------	| -----:|
| RentrantLock  		|   x   |
| ReadWriteLock         |       |
| StampedLock    		|       |
| Semaphore(1)          |       |