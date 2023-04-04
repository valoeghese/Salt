package valoeghese.salt.utils;

import java.util.function.BiConsumer;

/**
 * A bi-directional map.
 */
public interface BiMap<K, V> {
	/**
	 * Get the value associated with the given key.
	 * @param key the key to get the value of.
	 * @return the value associated with the given key.
	 */
	V getValue(K key);

	/**
	 * Get the key associated with the given value.
	 * @param value the value to get the key of.
	 * @return the key associated with the given value.
	 */
	K getKey(V value);

	/**
	 * Put a key-value pair into this map.
	 * @param key the key to put.
	 * @param value the value to put.
	 */
	void put(K key, V value);

	/**
	 * Remove a key-value pair from this map.
	 * @param key the key to remove.
	 */
	void removeKey(K key);

	/**
	 * Remove a key-value pair from this map.
	 * @param value the value to remove.
	 */
	void removeValue(V value);

	/**
	 * Iterate over all the key-value pairs in this bimap.
	 * @param consumer the consumer to accept the key-value pairs.
	 */
	void forEach(BiConsumer<K, V> consumer);

	/**
	 * Get the number of entries in this bimap.
	 * @return the number of entries in this bimap.
	 */
	int size();

	/**
	 * Put all the key-value pairs from the given bimap into this bimap.
	 * @param other the bimap to put all the key-value pairs from.
	 */
	default void putAll(BiMap<K, V> other) {
		other.forEach(this::put);
	}
}
