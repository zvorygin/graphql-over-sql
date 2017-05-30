package graphql.sql.core.config;

import javax.annotation.Nonnull;

public class TypeReferenceImpl implements TypeReference {
    @Nonnull
    private final String typeName;

    public TypeReferenceImpl(@Nonnull String typeName) {
        this.typeName = typeName;
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return typeName;
    }
}
