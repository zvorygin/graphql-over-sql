package graphql.sql.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FragmentExtractor {
    private final NodeExtractor nodeExtractor;
    private int[] fragmentTypePrimaryKeyRelativePositions;
    private final Map<String, ScalarExtractor> fieldExtractors = new LinkedHashMap<>();
    private final Map<String, NodeExtractor> referencedExtractors = new LinkedHashMap<>();
    private final List<FragmentExtractor> fragmentExtractors = new ArrayList<>();

    public FragmentExtractor(NodeExtractor nodeExtractor, int[] fragmentTypePrimaryKeyRelativePositions) {
        this.nodeExtractor = nodeExtractor != null ? nodeExtractor : (NodeExtractor) this;
        this.fragmentTypePrimaryKeyRelativePositions = fragmentTypePrimaryKeyRelativePositions;
    }

    public void addField(String name, ScalarExtractor extractor) {
        fieldExtractors.put(name, extractor);
    }

    public void addReference(String name, NodeExtractor nodeExtractor) {
        referencedExtractors.put(name, nodeExtractor);
    }

    public void addFragment(FragmentExtractor fragmentExtractor) {
        fragmentExtractors.add(fragmentExtractor);
    }

    /**
     * Checks if this fragment is applicable to current extraction type
     * @param node node to check type
     * @return <code>boolean</code> if this node matches fragment type, <code>false</code> otherwise.
     */
    protected boolean isApplicable(ResultNode node) {
        ArrayKey key = node.getKey();

        for (int position : fragmentTypePrimaryKeyRelativePositions) {
            if (key.at(position) != null) {
                return true;
            }
        }

        return false;
    }

    protected void extractTo(ResultSet rs, ResultNode resultNode, boolean extractFields) throws SQLException {
        if (extractFields) {
            for (Map.Entry<String, ScalarExtractor> extractorEntry : fieldExtractors.entrySet()) {
                resultNode.setField(extractorEntry.getKey(), extractorEntry.getValue().getValue(rs));
            }
        }

        for (FragmentExtractor fragmentExtractor : fragmentExtractors) {
            if (fragmentExtractor.isApplicable(resultNode)) {
                fragmentExtractor.extractTo(rs, resultNode, extractFields);
            }
        }

        for (Map.Entry<String, NodeExtractor> extractorEntry : referencedExtractors.entrySet()) {
            ArrayKey referenceKey = extractorEntry.getValue().getKey(rs);
            if (referenceKey != null) {
                Map<ArrayKey, ResultNode> references = resultNode.getReferences(extractorEntry.getKey());
                extractorEntry.getValue().extractTo(rs, references, referenceKey);
            }
        }
    }

    public int addKeyExtractor(ScalarExtractor extractor) {
        return nodeExtractor.addKeyExtractor(extractor);
    }

    public NodeExtractor getNodeExtractor() {
        return nodeExtractor;
    }
}
