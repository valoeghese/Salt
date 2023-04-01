package valoeghese.salt.io;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Database {
	private Database() {
	}

	private final Map<String, ProtoTable> protoTables = new HashMap<>();

	/**
	 * Read the given table as a map. The given column will be used as an index for row values.
	 */
	public <A, T> Map<A, T> readTable(String table, Class<T> rowType, String indexColumn, Class<A> indexType) throws IllegalArgumentException {
		try {
			return this.protoTables.get(table).parseAsMap(indexColumn, indexType, rowType);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Error parsing table " + table, e);
		}
	}

	/**
	 * Read the given table as a list of rows.
	 */
	public <T> List<T> readTable(String table, Class<T> rowType) throws IllegalArgumentException {
		try {
			return this.protoTables.get(table).parseAsList(rowType);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Error parsing table " + table, e);
		}
	}

	/**
	 * Read the given table as an object.
	 */
	public <T> T readTable(String table, Class<T> objectType, String keyColumn, String valueColumn) throws IllegalArgumentException {
		try {
			return this.protoTables.get(table).parseAsObject(objectType, keyColumn, valueColumn);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Error parsing table " + table, e);
		}
	}

	private static final Map<Class<?>, Function<String, ?>> PARSERS = new HashMap<>();

	public static <T> void registerParser(Class<T> clazz, Function<String, T> parser) {
		PARSERS.put(clazz, parser);
	}

	public static Database read(BufferedReader reader) throws IOException {
		String line;
		Database result = new Database();
		ProtoTable reading = null;

		while ((line = reader.readLine()) != null) {
			if (!line.isBlank()) {
				if (line.charAt(0) == '\t') {
					if (reading == null) throw new IllegalArgumentException("File contains indented lines that belong to no table!");

					line = line.trim();
					Map<String, Object> row = new HashMap<>();
					String[] values = line.split("\\s+");

					final int columns = reading.headers.length;

					for (int i = 0; i < values.length; i++) {
						// todo use some sort of array splice to bulk do it, if faster?
						if (i > columns) {
							((List<String>) row.get(reading.headers[columns - 1])).add(values[i]);
						} else if (i == columns) {
							List<String> value = new ArrayList<>();
							value.add(values[i - 1]);
							value.add(values[i]);

							row.put(reading.headers[columns - 1], value);

						} else {
							row.put(reading.headers[i], values[i]);
						}
					}

					// Add the row to the current proto-table being read
					reading.rows.add(row);
				}
				else {
					line = line.trim();

					List<String> headers = new LinkedList<>(Arrays.asList(line.split("\\s+")));

					String tableName = headers.remove(0);
					reading = new ProtoTable(tableName, headers.toArray(String[]::new));
					result.protoTables.put(tableName, reading);
				}
			}
		}

		return result;
	}

	private static class ProtoTable {
		private ProtoTable(String name, String[] headers) {
			this.name = name;
			this.headers = headers;
		}

		private final String name;
		private final String[] headers;
		private final List<Map<String, Object>> rows = new ArrayList<>();

		public <A, T> Map<A, T> parseAsMap(String indexColumn, Class<A> indexType, Class<T> rowType) throws ReflectiveOperationException, IllegalArgumentException {
			Map<A, T> table = new LinkedHashMap<>();

			for (Map<String, Object> protoRow : this.rows) {
				Map.Entry<A, T> parsedRow = parseRow(protoRow, indexColumn, indexType, rowType);
				table.put(parsedRow.getKey(), parsedRow.getValue());
			}

			return table;
		}

		public <T> T parseAsObject(Class<T> objectType, String keyColumn, String valueColumn) throws ReflectiveOperationException, IllegalArgumentException {
			T object = objectType.getDeclaredConstructor().newInstance();

			for (Map<String, Object> protoRow : this.rows) {
				Field field = getFieldBySerialisedName(objectType, protoRow.get(keyColumn).toString());
				field.setAccessible(true);

				Object parsed = parse(field, protoRow.get(valueColumn));
				field.set(object, parsed);
			}

			return object;
		}

		public <T> List<T> parseAsList(Class<T> rowType) throws ReflectiveOperationException, IllegalArgumentException {
			List<T> list = new LinkedList<>();

			for (Map<String, Object> protoRow : this.rows) {
				Map.Entry<Object, T> parsedRow = parseRow(protoRow, null, Object.class, rowType);
				list.add(parsedRow.getValue());
			}

			return list;
		}

		private <A, T> Map.Entry<A, T> parseRow(Map<String, Object> protoRow, @Nullable String indexColumn, Class<A> indexType, Class<T> rowType) throws ReflectiveOperationException, IllegalArgumentException {
			T row = rowType.getDeclaredConstructor().newInstance();
			A key = null;

			for (var entry : protoRow.entrySet()) {
				Field field = getFieldBySerialisedName(rowType, entry.getKey());
				field.setAccessible(true);
				Object parsed = parse(field, entry.getValue());

				if (entry.getKey().equals(indexColumn)) {
					// indexType super parsed.getClass()
					if (indexType.isAssignableFrom(parsed.getClass())) {
						key = (A) parsed;
					}
					else {
						throw new IllegalArgumentException("Value \"" + entry.getValue() + "\" in index column " + indexColumn + " not of specified index type!");
					}
				}

				field.set(row, parsed);
			}

			if (key == null && indexColumn != null) {
				throw new IllegalArgumentException("No value in column " + indexColumn + " present for a row.");
			}

			return new AbstractMap.SimpleEntry<>(key, row);
		}

		private static Object parse(Field field, Object value) throws NullPointerException {
			try {
				if (value instanceof String stringValue) {
					return parse(field.getType(), stringValue);
				} else if (value instanceof List<?> protoValues) {
					List<Object> parsedValues = new ArrayList<>(protoValues.size());

					// parse each value in the list
					for (Object protoValue : protoValues) {
						parsedValues.add(parse(getFirstGenericClass(field), (String) protoValue));
					}

					return parsedValues;
				} else {
					throw new IllegalStateException("wtf (A will-never-happen case triggered: proto-row contains a non-string non-list value. Contact a developer.");
				}
			} catch (NullPointerException e) {
				throw new IllegalArgumentException("Cannot parse type " + (value instanceof List<?> ? getFirstGenericClass(field) : field.getType()));
			}
		}

		private static Object parse(Class<?> clazz, String value) {
			return PARSERS.get(clazz).apply(value);
		}

		private static Class<?> getFirstGenericClass(Field field) {
			ParameterizedType generics = (ParameterizedType) field.getGenericType();
			return (Class<?>) generics.getActualTypeArguments()[0];
		}

		private Field getFieldBySerialisedName(Class<?> rowType, String serialisedName) throws IllegalArgumentException {
			// TODO cache this?
			try {
				// Attempt 1: get field with exact name
				return rowType.getDeclaredField(serialisedName);
			}
			catch (NoSuchFieldException e) {
				// Attempt 2: scan fields for serialised name.
				for (Field field : rowType.getDeclaredFields()) {
					if (field.isAnnotationPresent(SerialisedName.class)) {
						SerialisedName actualSerialisedName = field.getDeclaredAnnotation(SerialisedName.class);

						if (serialisedName.equals(actualSerialisedName.value())) {
							return field;
						}
					}
				}
			}

			// if could not find a suitable field, code reaches here.
			throw new IllegalArgumentException("Could not find field with serialised name \"" + serialisedName + "\" in class " + rowType);
		}
	}

	static {
		registerParser(byte.class, Byte::parseByte);
		registerParser(short.class, Short::parseShort);
		registerParser(int.class, Integer::parseInt);
		registerParser(long.class, Long::parseLong);
		registerParser(float.class, Float::parseFloat);
		registerParser(double.class, Double::parseDouble);
		registerParser(String.class, s -> s);
		registerParser(boolean.class, Boolean::parseBoolean);
		registerParser(char.class, s -> s.charAt(0));
	}
}
