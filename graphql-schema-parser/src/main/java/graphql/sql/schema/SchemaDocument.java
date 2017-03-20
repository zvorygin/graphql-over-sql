package graphql.sql.schema;

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
    Map<String, ? extends ObjectType> getTypes();

    /**
     * @return all GraphQL interfaces defined in this document
     */
    @Nonnull
    Map<String, ? extends Interface> getInterfaces();

    /**
     * @return all scalars defined in this document
     */
    @Nonnull
    Map<String, ? extends Scalar> getScalars();

    /**
     * @return all input types defined in this document
     */
    @Nonnull
    Map<String, ? extends Input> getInputs();
}
