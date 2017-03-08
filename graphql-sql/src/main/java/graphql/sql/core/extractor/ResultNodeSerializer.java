package graphql.sql.core.extractor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class ResultNodeSerializer extends JsonSerializer<ResultNode> {
    @Override
    public void serialize(ResultNode node, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        for (Map.Entry<String, Object> entry : node.getFields().entrySet()) {
            jsonGenerator.writeFieldName(entry.getKey());
            jsonGenerator.writeObject(entry.getValue());
        }
        for (Map.Entry<String, ResultNode> entry : node.getCompositeFields().entrySet()) {
            jsonGenerator.writeFieldName(entry.getKey());
            serialize(entry.getValue(), jsonGenerator, serializerProvider);
        }
        for (Map.Entry<String, Map<ArrayKey, ResultNode>> entry : node.getReferences().entrySet()) {
            jsonGenerator.writeFieldName(entry.getKey());
            jsonGenerator.writeStartArray();
            for (ResultNode resultNode : entry.getValue().values()) {
                serialize(resultNode, jsonGenerator, serializerProvider);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();
    }
}
