package com.codewise.lock;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ConcurrentSoftValueMap<K, V> extends AbstractMap<K, V> {
	private static final long serialVersionUID = -759186495668191195L;
	private final boolean equalsHashContract;
	private final ConcurrentHashMap<Key, V> map;
	private final ReferenceQueue<Object> queue;

	public ConcurrentSoftValueMap() {
		this(true);
	}

	public ConcurrentSoftValueMap(boolean equalsHashContract) {
		this.equalsHashContract = equalsHashContract;
		this.map = new ConcurrentHashMap<>();
		this.queue = new ReferenceQueue<>();
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction);
		V oldValue = get(key);

		V newValue = remappingFunction.apply(key, oldValue);
		if (newValue == null) {
			// delete mapping
			if (oldValue != null || containsKey(key)) {
				// something to remove
				remove(key);
				return null;
			} else {
				// nothing to do. Leave things as they were.
				return null;
			}
		} else {
			// add or replace old mapping
			return put(key, newValue);
		}
	}

	@Override
	public V get(Object key) {
		return this.map.get(decorateKey(key));
	}

	private Key decorateKey(Object key) {
		return new Key(key, this.queue, equalsHashContract);
	}

	@Override
	public V put(K key, V value) {
		releaseUnreferencedData();
		Key kk = decorateKey(key);
		V newValue = this.map.putIfAbsent(kk, value);
		return (newValue == null) ? value : newValue;
	}

	@Override
	public int size() {
		releaseUnreferencedData();
		return this.map.size();
	}

	private final void releaseUnreferencedData() {
		Key key = null;
		while ((key = (Key) this.queue.poll()) != null) {
			this.map.remove(key);
		}
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	private static final class Key extends SoftReference<Object> {
		private final int hashCode;

		public Key(Object key, ReferenceQueue<Object> queue, boolean equalsHashContract) {
			super(key, queue);
			this.hashCode = equalsHashContract ? key.hashCode() : 1;
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (object instanceof Key) {
				Key candidate = (Key) object;
				Object current = this.get();
				return (current != null && current.equals(candidate.get()));
			}
			return false;
		}
	}
}
