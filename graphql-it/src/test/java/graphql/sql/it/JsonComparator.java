package graphql.sql.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Comparator;

public class JsonComparator implements Comparator<JsonNode> {
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
