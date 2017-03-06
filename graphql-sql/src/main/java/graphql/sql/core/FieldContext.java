package graphql.sql.core;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.sql.core.extractor.NodeExtractor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class FieldContext {
    @Nonnull
    private final String query;

    @Nonnull
    private final NodeExtractor nodeExtractor;

    @Nonnull
    private final Map<String, QueryPreparer.PlaceHolder> placeHolders;

    @Nonnull
    private final Collection<QueryPreparer.StaticPlaceHolder> staticPlaceHolders;

    public FieldContext(@Nonnull String query,
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
}
