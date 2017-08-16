package graphql.sql.schema.parser.impl;

import graphql.sql.core.config.TypeReference;
import graphql.sql.schema.parser.SchemaAnnotation;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaFieldArgument;

import java.util.Collections;
import java.util.Map;

class SchemaFieldImpl extends NamedSchemaNode implements SchemaField {
    private final TypeReference typeReference;
    private final Map<String, SchemaFieldArgumentImpl> arguments;
    private final Map<String, SchemaAnnotationImpl> annotations;

    public SchemaFieldImpl(String name,
                           Location location,
                           TypeReference typeReference,
                           Map<String, SchemaAnnotationImpl> annotations) {
        this(name, location, typeReference, Collections.emptyMap(), annotations);
    }

    public SchemaFieldImpl(String name,
                           Location location,
                           TypeReference typeReference,
                           Map<String, SchemaFieldArgumentImpl> arguments,
                           Map<String, SchemaAnnotationImpl> annotations) {
        super(name, location);
        this.typeReference = typeReference;
        this.arguments = arguments;
        this.annotations = annotations;
    }

    @Override
    public TypeReference getType() {
        return typeReference;
    }

    @Override
    public Map<String, SchemaFieldArgument> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }

    @Override
    public Map<String, SchemaAnnotation> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }
}
