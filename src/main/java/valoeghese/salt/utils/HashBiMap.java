package valoeghese.salt.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

public class HashBiMap<K, V> implements BiMap<K, V> {
	public HashBiMap() {
	}

	/**
	 * Creates a new HashBiMap with the same key-value mappings as the given Map.
	 * @param map the Map to copy.
	 */
	public HashBiMap(Map<K, V> map) {
		map.forEach(this::put);
	}

	/**
	 * Creates a new HashBiMap with the same key-value mappings as the given Map.
	 * @param reverseMap the Map to copy.
	 * @param reverse whether the given Map is the reverse map.
	 */
	public HashBiMap(Map<V, K> reverseMap, boolean reverse) {
		if (reverse) {
			reverseMap.forEach((v, k) -> this.put(k, v));
		} else {
			throw new IllegalArgumentException("This constructor is only for reverse maps");
		}
	}

	/**
	 * Creates a new HashBiMap with the same key-value mappings as the given BiMap.
	 * @param biMap the BiMap to copy.
	 */
	public HashBiMap(BiMap<K, V> biMap) {
		biMap.forEach(this::put);
	}

	private final HashMap<K, V> map = new HashMap<>();
	private final HashMap<V, K> reverseMap = new HashMap<>();

	@Override
	public V getValue(K key) {
		return this.map.get(key);
	}

	@Override
	public K getKey(V value) {
		return this.reverseMap.get(value);
	}

	@Override
	public void put(K key, V value) {
		this.map.put(key, value);
		this.reverseMap.put(value, key);
	}

	@Override
	public void removeKey(K key) {
		this.reverseMap.remove(this.map.remove(key));
	}

	@Override
	public void removeValue(V value) {
		this.map.remove(this.reverseMap.remove(value));
	}

	@Override
	public void forEach(BiConsumer<K, V> consumer) {
		this.map.forEach(consumer::accept);
	}

	@Override
	public int size() {
		return this.map.size();
	}

	public static <T, K, V> Collector<T, ?, HashBiMap<K, V>> collectToHashBiMap(Function<T, K> keyMapper, Function<T, V> valueMapper) {
		return Collector.of(HashBiMap::new, (map, t) -> map.put(keyMapper.apply(t), valueMapper.apply(t)), (a, b) -> {
			a.putAll(b);
			return a;
		});
	}
}
