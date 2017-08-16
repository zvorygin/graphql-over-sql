package graphql.sql.schema.parser;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents GraphQL schema document.
 */
public interface SchemaDocument {

    /**
     * @return the only schema defined in this document
     */
    @Nonnull
    Schema getSchema();

    /**
     * @return all GraphQL types defined in this document
     */
    @Nonnull
    Map<String, ? extends SchemaObjectType> getTypes();

    /**
     * @return all GraphQL interfaces defined in this document
     */
    @Nonnull
    Map<String, ? extends SchemaInterface> getInterfaces();

    /**
     * @return all scalars defined in this document
     */
    @Nonnull
    Map<String, ? extends SchemaScalar> getScalars();

    /**
     * @return all input types defined in this document
     */
    @Nonnull
    Map<String, ? extends SchemaInput> getInputs();
}
