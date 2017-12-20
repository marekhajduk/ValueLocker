package com.codewise.lock;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentWeakValueMap<K, V> extends ConcurrentHashMap<K, V> {
	private static final long serialVersionUID = -759186495668191195L;
	private final boolean equalsHashContract;
	private final ConcurrentHashMap<Key, V> map;
	private final ReferenceQueue<Object> queue;
	
	public ConcurrentWeakValueMap() {
		this(true);
	}
	
	public ConcurrentWeakValueMap(boolean equalsHashContract )  {
		this.equalsHashContract = equalsHashContract;
		this.map = new ConcurrentHashMap<Key, V>();
		this.queue = new ReferenceQueue<Object>();
	}
	
	@Override
	public V get(Object key) {
		return this.map.get(new Key(key, this.queue, equalsHashContract));
	}
	
	//TODO - add way of comparable objects -> cause O(logn) complexity on hascode conflicts
	@Override
	public V put(K key, V value) {
		releaseUnreferencedData();
		return this.map.put(new Key(key, this.queue, equalsHashContract), value);
	}
	
	@Override
	public int size() {
		releaseUnreferencedData();
		return this.map.size();
	}
	
	private final void releaseUnreferencedData() {
		Key key = null;
		
		while((key = (Key)this.queue.poll()) != null) {
			this.map.remove(key);
		}
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}
	
	private final static class Key extends WeakReference<Object> {
		private final int hashCode;
		
		public Key(Object key, ReferenceQueue<Object> queue, boolean equalsHashContract) {
			super(key,queue);
			this.hashCode = equalsHashContract ? key.hashCode() : 1;
		}
		
		@Override
		public int hashCode() {
			return this.hashCode;
		}
		
		@Override
		public boolean equals(Object object) {
			
			if(this == object) {
				return true;
			}
			
			if(object instanceof Key) {
				Key candidate = (Key)object;
				Object current = get();
				return (current != null && current.equals(candidate.get()));
			}
			return false;
		}
	}
}
