package magic.data.impl;

import java.util.Iterator;
import java.util.Map;

import magic.data.PersistentHashSet;
import magic.data.APersistentSet;

/**
 * Wrapper for the key set of a persistent map
 * @author Mike
 *
 * @param <K>
 * @param <V>
 */
public final class KeySetWrapper<K,V> extends BasePersistentSet<K> {
	private static final long serialVersionUID = -3297453356838115646L;

	
	APersistentSet<Map.Entry<K,V>> source;
	
	
	public KeySetWrapper(APersistentSet<Map.Entry<K, V>> base) {
		source=base;
	}
	
	@Override
	public APersistentSet<K> include(K value) {
		return PersistentHashSet.coerce(this).include(value);
	}

	@Override
	public int size() {
		return source.size();
	}

	@Override
	public Iterator<K> iterator() {
		return new KeySetIterator<K, V>(source);
	}
	
	public static class KeySetIterator<K,V> implements Iterator<K> {
		private Iterator<Map.Entry<K,V>> source;
		
		public KeySetIterator(APersistentSet<Map.Entry<K,V>> base) {
			source=base.iterator();
		}

		@Override
		public boolean hasNext() {
			return source.hasNext();
		}

		@Override
		public K next() {
			Map.Entry<K,V> next=source.next();
			return next.getKey();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
