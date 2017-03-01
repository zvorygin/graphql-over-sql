package graphql.sql.core;

import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.config.domain.ReferenceType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlResultProcessor {

    private final QueryNodeProcessor root;

    public SqlResultProcessor(QueryNodeProcessor root) {
        this.root = root;
    }

    Collection<ResultNode> process(ResultSet rs) throws SQLException {
        Map<ResultKey, ResultNode> result = new LinkedHashMap<>();
        while (rs.next()) {
            ResultNode row = findOrCreateNode(rs, result, root);
            processNodeChildren(row, root, rs);
        }
        return result.values();
    }

    private void processNodeChildren(ResultNode node, QueryNodeProcessor processor, ResultSet rs)
            throws SQLException {

        for (Map.Entry<EntityReference, QueryNodeProcessor> entry : processor.getReferencesProcessors().entrySet()) {
            EntityReference reference = entry.getKey();
            QueryNodeProcessor childProcessor = entry.getValue();
            if (reference.getReferenceType() == ReferenceType.MANY_TO_ONE) {
                Map<EntityReference, ResultNode> manyToOneReferences = node.getManyToOneReferences();
                ResultNode child = manyToOneReferences.get(reference);
                assert child == null || child.getKey().equals(childProcessor.getKey(rs));
                if (child == null) {
                    ResultKey childKey = childProcessor.getKey(rs);
                    if (childKey != null) {
                        child = createResultNode(childKey, childProcessor, rs);
                        manyToOneReferences.put(reference, child);
                    }
                }

                if (child != null) {
                    processNodeChildren(child, childProcessor, rs);
                }

            } else if (reference.getReferenceType() == ReferenceType.ONE_TO_MANY) {
                ResultKey childKey = childProcessor.getKey(rs);
                if (childKey == null)
                    continue;

                Map<EntityReference, Map<ResultKey, ResultNode>> oneToManyReferences = node.getOneToManyReferences();
                Map<ResultKey, ResultNode> referencedCollection =
                        oneToManyReferences.computeIfAbsent(reference, k -> new LinkedHashMap<>());
                ResultNode child = referencedCollection.get(childKey);
                if (child == null) {
                    child = createResultNode(childKey, childProcessor, rs);
                    referencedCollection.put(childKey, child);
                }
                processNodeChildren(child, childProcessor, rs);
            } else {
                // Move that to reference types?
                throw new IllegalStateException(
                        String.format("Unknown reference type [%s]", reference.getReferenceType()));
            }
        }
    }

    private ResultNode findOrCreateNode(ResultSet rs,
                                        Map<ResultKey, ResultNode> nodes,
                                        QueryNodeProcessor processor) throws SQLException {
        ResultKey rowKey = processor.getKey(rs);

        ResultNode node = nodes.get(rowKey);

        if (node != null) {
            return node;
        }

        node = createResultNode(rowKey, processor, rs);
        nodes.put(rowKey, node);

        return node;
    }

    private ResultNode createResultNode(ResultKey rowKey, QueryNodeProcessor processor, ResultSet rs)
            throws SQLException {
        ResultNode node = new ResultNode(rowKey);
        // We need to process node fields only once for single node, since fields are the same for all joined rows.
        for (FieldProcessor fieldProcessor : processor.getFieldProcessors()) {
            fieldProcessor.process(rs, node);
        }
        return node;
    }
}
