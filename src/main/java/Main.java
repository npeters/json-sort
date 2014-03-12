import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Lf2SpacesIndenter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

public class Main {
	public static void main(String[] args) throws JsonParseException,
			IOException {
		String in;

		if (args.length == 0) {
			System.out.println(" [filename] or - ");
			System.exit(0);
		}
		InputStream stream;
		if ("-".equals(args[0])) {
			stream = System.in;
		} else {
			stream = new FileInputStream(args[0]);
		}
		in = CharStreams.toString(new InputStreamReader(stream, Charset
				.forName("UTF-8")));
		JsonFactory factory = new JsonFactory();
		factory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		JsonParser jp = factory.createParser(in);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode tree = mapper.readTree(jp);
		StringWriter writer = new StringWriter();
		JsonGenerator generator = factory.createGenerator(writer);
		generator.enable(Feature.AUTO_CLOSE_JSON_CONTENT);
		generator.enable(Feature.FLUSH_PASSED_TO_STREAM);
		generator.enable(Feature.AUTO_CLOSE_TARGET);
		sort(tree);

		DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		pp.indentArraysWith(new Lf2SpacesIndenter());

		System.out.println(mapper.writer(pp).writeValueAsString(tree));

	}

	private static void sort(JsonNode tree) {

		if (tree.isObject()) {
			sortObject(tree);
		} else if (tree.isArray()) {
			sortArray(tree);
		}

	}

	private static void sortArray(JsonNode tree) {

		for (JsonNode jsonNode : tree) {
			sort(jsonNode);
		}

		List<JsonNode> list = Lists.newArrayList(((ArrayNode) tree).iterator());
		Collections.sort(list, new JsonNodeComparator());
		((ArrayNode) tree).removeAll();
		((ArrayNode) tree).addAll(list);
	}

	private static void sortObject(JsonNode tree) {
		List<String> asList = Lists.newArrayList(tree.fieldNames());
		Collections.sort(asList);
		LinkedHashMap<String, JsonNode> map = new LinkedHashMap<String, JsonNode>();
		for (String f : asList) {

			JsonNode value = tree.get(f);
			sort(value);
			map.put(f, value);
		}
		((ObjectNode) tree).removeAll();
		((ObjectNode) tree).setAll(map);
	}

	// private static void gen(JsonNode tree, JsonGenerator generator)
	// throws JsonGenerationException, IOException {
	//
	// if (tree.isObject()) {
	// genObject(tree, generator);
	// } else if (tree.isArray()) {
	// genArray(tree, generator);
	// } else if (tree.isValueNode()) {
	// genValue(tree, generator);
	// }
	//
	// }
	//
	// private static void genValue(JsonNode tree, JsonGenerator generator)
	// throws JsonGenerationException, IOException {
	// generator.writeString(tree.asText());
	// }
	//
	// private static void genArray(JsonNode tree, JsonGenerator generator)
	// throws JsonGenerationException, IOException {
	// generator.writeStartArray();
	// List<JsonNode> list = Lists.newArrayList(tree.elements());
	// for (JsonNode jsonNode : list) {
	// gen(jsonNode, generator);
	// }
	//
	// generator.writeEndArray();
	// }
	//
	// private static void genObject(JsonNode tree, JsonGenerator generator)
	// throws IOException, JsonGenerationException {
	// generator.writeStartObject();
	//
	// for (Iterator<String> iterator = tree.fieldNames(); iterator.hasNext();)
	// {
	// String f = iterator.next();
	// JsonNode fValue = tree.get(f);
	// generator.writeFieldName(f);
	// gen(fValue, generator);
	// }
	//
	// generator.writeEndObject();
	// }

	public static class JsonNodeComparator implements Comparator<JsonNode> {
		public int compare(JsonNode o1, JsonNode o2) {
			if (o1 == null && o2 == null) {
				return 0;
			}

			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}

			if (o1.isObject() && o2.isObject()) {
				return compObject(o1, o2);
			} else if (o1.isArray() && o2.isArray()) {
				return compArray(o1, o2);
			} else if (o1.isValueNode() && o2.isValueNode()) {
				return compValue(o1, o2);
			} else {
				return 1;
			}
		}

		private int compValue(JsonNode o1, JsonNode o2) {

			if (o1.isNull()) {
				return -1;
			}

			if (o2.isNull()) {
				return 1;
			}

			if (o1.isNumber() && o2.isNumber()) {
				return o1.bigIntegerValue().compareTo(o2.bigIntegerValue());
			}

			return o1.asText().compareTo(o2.asText());
		}

		private int compArray(JsonNode o1, JsonNode o2) {

			int c = ((ArrayNode) o1).size() - ((ArrayNode) o2).size();
			if (c != 0) {
				return c;
			}
			for (int i = 0; i < ((ArrayNode) o1).size(); i++) {
				c = compare(o1.get(i), o2.get(i));
				if (c != 0) {
					return c;
				}
			}

			return 0;
		}

		private int compObject(JsonNode o1, JsonNode o2) {

			String id1 = o1.get("id") == null ? null : o1.get("id").asText();
			String id2 = o2.get("id") == null ? null : o2.get("id").asText();
			if (id1 != null) {
				int c = id1.compareTo(id2);
				if (c != 0) {
					return c;
				}
			}
			int c = ((ObjectNode) o1).size() - ((ObjectNode) o2).size();
			if (c != 0) {
				return c;
			}

			Iterator<String> fieldNames1 = ((ObjectNode) o1).fieldNames();
			Iterator<String> fieldNames2 = ((ObjectNode) o2).fieldNames();
			for (; fieldNames1.hasNext();) {
				String f = fieldNames1.next();

				c = f.compareTo(fieldNames2.next());
				if (c != 0) {
					return c;
				}

				JsonNode n1 = o1.get(f);
				JsonNode n2 = o2.get(f);
				c = compare(n1, n2);
				if (c != 0) {
					return c;
				}
			}
			return 0;
		}
	}
}
