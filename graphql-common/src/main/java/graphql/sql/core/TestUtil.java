package graphql.sql.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public final class TestUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter WRITER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
    private static final JsonComparator JSON_COMPARATOR = new JsonComparator(WRITER);

    private TestUtil() {
    }

    public static void sortArrays(JsonNode node) {
        if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            ArrayList<JsonNode> children = Lists.newArrayList(arrayNode.iterator());
            children.iterator().forEachRemaining(TestUtil::sortArrays);
            children.sort(JSON_COMPARATOR);
            for (int i = 0; i < children.size(); i++) {
                arrayNode.set(i, children.get(i));
            }
        } else if (node instanceof ObjectNode) {
            node.iterator().forEachRemaining(TestUtil::sortArrays);
        }
    }

    public static JsonNode readResource(File file) throws IOException {
        return OBJECT_MAPPER.readTree(file);
    }

    public static <T> T readResource(File file, TypeReference<T> typeReference) throws IOException {
        return OBJECT_MAPPER.readValue(file, typeReference);
    }

    public static void assertEquals(JsonNode expected, JsonNode response) throws JsonProcessingException {
        Assert.assertEquals(WRITER.writeValueAsString(expected), WRITER.writeValueAsString(response));
    }

    public static JsonNode valueToTree(Object o) {
        return OBJECT_MAPPER.valueToTree(o);
    }

    public static void addMixIn(Class<?> typeClass, Class<?> nodeMixInClass) {
        OBJECT_MAPPER.addMixIn(typeClass, nodeMixInClass);
    }

    public static final class JsonComparator implements Comparator<JsonNode> {
        private final ObjectWriter writer;

        public JsonComparator(ObjectWriter writer) {
            this.writer = writer;
        }

        @Override
        public int compare(JsonNode o1, JsonNode o2) {
            try {
                return writer.writeValueAsString(o1).compareTo(writer.writeValueAsString(o2));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
