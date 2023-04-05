package valoeghese.salt.utils;

import java.util.LinkedHashMap;

public class Builders {
	/**
	 * Creates a builder for a new linked hash map.
	 * @param <K> the type of the key for this linked hash map.
	 * @param <V> the type of the value for this linked hash map.
	 * @return the linked hash map builder.
	 */
	public static <K, V> LinkedHashMapBuilder<K, V> newLinkedHashMap() {
		return new LinkedHashMapBuilder<>();
	}

	/**
	 * Builder for a linked hash map.
	 * @param <K> the type of the key for this linked hash map.
	 * @param <V> the type of the value for this linked hash map.
	 */
	public static class LinkedHashMapBuilder<K, V> {
		private LinkedHashMapBuilder() {
			// restrict scope
		}

		private final LinkedHashMap<K, V> map = new LinkedHashMap<>();

		public LinkedHashMapBuilder<K, V> put(K key, V value) {
			this.map.put(key, value);
			return this;
		}

		public LinkedHashMap<K, V> build() {
			return this.map;
		}
	}
}
