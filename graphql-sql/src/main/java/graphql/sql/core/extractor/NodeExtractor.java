package graphql.sql.core.extractor;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodeExtractor extends FragmentExtractor {
    public static final int[] EMPTY = new int[0];
    private final List<ScalarExtractor> keyExtractors = new ArrayList<>();

    public NodeExtractor() {
        super(null, EMPTY);
    }

    public int addKeyExtractor(ScalarExtractor keyExtractor) {
        // If this extractor already exists in key
        for (int i = 0; i < keyExtractors.size(); i++) {
            if (keyExtractors.get(i).getPosition() == keyExtractor.getPosition()) {
                return i;
            }
        }
        keyExtractors.add(keyExtractor);
        return keyExtractors.size() - 1;
    }

    public void extractTo(ResultSet rs, Map<ArrayKey, ResultNode> result, ArrayKey key) throws SQLException {

        ResultNode resultNode = result.get(key);
        if (resultNode == null) {
            resultNode = new ResultNode(key);
            extractTo(rs, resultNode, true);
            result.put(key, resultNode);
        } else {
            extractTo(rs, resultNode, false);
        }
    }

    public ResultNode extract(ResultSet rs) throws SQLException {
        ResultNode result = new ResultNode(getKey(rs));
        extractTo(rs, result, true);
        return result;
    }

    @Nullable
    public ArrayKey getKey(ResultSet rs) throws SQLException {
        Object[] key = new Object[keyExtractors.size()];
        int i = 0;
        for (ScalarExtractor keyExtractor : keyExtractors) {
            key[i++] = keyExtractor.getValue(rs);
        }
        return ArrayKey.createArrayKey(key);
    }
}
