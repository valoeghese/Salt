package valoeghese.salt.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
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

	public <A, T> Map<A, T> readTable(String table, Class<T> rowType, String indexColumn, Class<A> indexType) {
		try {
			return this.protoTables.get(table).readTable(indexColumn, indexType, rowType);
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
				}
				else {
					line = line.trim();

					List<String> headers = new LinkedList<>(Arrays.asList(line.split("\\s+")));

					String tableName = headers.remove(0);
					reading = new ProtoTable(tableName, headers.toArray(String[]::new));
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

		public <A, T> Map<A, T> readTable(String indexColumn, Class<A> indexType, Class<T> rowType) throws ReflectiveOperationException, IllegalArgumentException {
			Map<A, T> table = new LinkedHashMap<>();

			for (Map<String, Object> protoRow : this.rows) {
				T row = rowType.getDeclaredConstructor().newInstance();
				A key = null;

				for (var entry : protoRow.entrySet()) {
					Field field = rowType.getDeclaredField(entry.getKey());
					field.setAccessible(true);
					Object parsed;

					try {
						if (entry.getValue() instanceof String value) {
							parsed = parse(field, value);
						} else if (entry.getValue() instanceof List<?> protoValues) {
							List<Object> parsedValues = new ArrayList<>(protoValues.size());

							// parse each value in the list
							for (Object value : protoValues) {
								parsedValues.add(parse(field, (String) value));
							}

							parsed = parsedValues;
						} else {
							throw new IllegalStateException("wtf (A will-never-happen case triggered: proto-row contains a non-string non-list value. Contact a developer.");
						}
					} catch (NullPointerException e) {
						throw new IllegalArgumentException("Cannot parse type " + field.getType());
					}

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

				if (key == null) {
					throw new IllegalArgumentException("No value in column " + indexColumn + " present for a row.");
				}

				table.put(key, row);
			}

			return table;
		}

		private static Object parse(Field field, String value) {
			return PARSERS.get(field.getType()).apply(value);
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
