package graphql.sql.core;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.sql.core.extractor.ArrayKey;
import graphql.sql.core.extractor.NodeExtractor;
import graphql.sql.core.extractor.ResultNode;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlFieldExecutor implements FieldExecutor {
    @Nonnull
    private final String query;

    @Nonnull
    private final NodeExtractor nodeExtractor;

    @Nonnull
    private final Map<String, QueryPreparer.PlaceHolder> placeHolders;

    @Nonnull
    private final Collection<QueryPreparer.StaticPlaceHolder> staticPlaceHolders;

    public SqlFieldExecutor(@Nonnull String query,
                            @Nonnull NodeExtractor nodeExtractor,
                            @Nonnull Map<String, QueryPreparer.PlaceHolder> placeHolders,
                            @Nonnull Collection<QueryPreparer.StaticPlaceHolder> staticPlaceHolders) {
        this.query = query;
        this.nodeExtractor = nodeExtractor;
        this.placeHolders = placeHolders;
        this.staticPlaceHolders = staticPlaceHolders;
    }

    @Nonnull
    public String getQuery() {
        return query;
    }

    @Nonnull
    public NodeExtractor getNodeExtractor() {
        return nodeExtractor;
    }

    @Nonnull
    public Map<String, QueryPreparer.PlaceHolder> getPlaceHolders() {
        return placeHolders;
    }

    @Nonnull
    public Collection<QueryPreparer.StaticPlaceHolder> getStaticPlaceHolders() {
        return staticPlaceHolders;
    }

    ResultSet setParametersAndExecute(PreparedStatement ps, Map<String, Object> variables)
            throws SQLException {

        for (Map.Entry<String, QueryPreparer.PlaceHolder> entry : getPlaceHolders().entrySet()) {
            entry.getValue().setObject(variables.get(entry.getKey()), ps);
        }

        for (QueryPreparer.StaticPlaceHolder placeHolder : getStaticPlaceHolders()) {
            placeHolder.setValue(ps);
        }

        return ps.executeQuery();
    }

    @Override
    @Nonnull
    public Object execute(Connection conn, Map<String, Object> variables) throws SQLException {
        Map<ArrayKey, ResultNode> response = new LinkedHashMap<>();
        System.out.println(getQuery());
        try (PreparedStatement ps = conn.prepareStatement(getQuery());
             ResultSet rs = setParametersAndExecute(ps, variables)) {
            NodeExtractor extractor = getNodeExtractor();
            while (rs.next()) {
                ArrayKey key = extractor.getKey(rs);
                extractor.extractTo(rs, response, key);
            }

        }
        return response.values();
    }
}
