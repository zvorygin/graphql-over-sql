package graphql.sql.schema.engine;


import graphql.sql.schema.parser.SchemaTypeReference;

import javax.annotation.Nonnull;

public final class Field {
    @Nonnull
    private final String name;

    @Nonnull
    private final SchemaTypeReference typeReference;

    public Field(@Nonnull String name, @Nonnull SchemaTypeReference typeReference) {
        this.name = name;
        this.typeReference = typeReference;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public SchemaTypeReference getTypeReference() {
        return typeReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Field field = (Field) o;

        return name.equals(field.name) && typeReference.equals(field.typeReference);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeReference.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", typeReference=" + typeReference +
                '}';
    }
}
