package valoeghese.salt.utils;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

public class HashBiMap<K, V> implements BiMap<K, V> {
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
