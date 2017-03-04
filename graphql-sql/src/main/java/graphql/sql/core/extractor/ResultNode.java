package graphql.sql.core.extractor;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonSerialize(using = ResultNodeSerializer.class)
public class ResultNode {
    private final ArrayKey key;

    private final Map<String, Object> fields = new LinkedHashMap<>();

    private final Map<String, Map<ArrayKey, ResultNode>> references = new LinkedHashMap<>();

    public ResultNode(ArrayKey key) {
        this.key = key;
    }

    public void setField(String key, Object value) {
        fields.put(key, value);
    }

    public ArrayKey getKey() {
        return key;
    }

    public Map<ArrayKey, ResultNode> getReferences(String referenceName) {
        return references.computeIfAbsent(referenceName, (key) -> new LinkedHashMap<>());
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public Map<String, Map<ArrayKey, ResultNode>> getReferences() {
        return references;
    }
}
