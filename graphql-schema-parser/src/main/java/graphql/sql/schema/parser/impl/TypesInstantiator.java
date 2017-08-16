package graphql.sql.schema.parser.impl;

import graphql.sql.core.config.TypeReference;
import graphql.sql.schema.antlr.GraphqlSchemaBaseVisitor;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import graphql.sql.schema.parser.SchemaParserException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class TypesInstantiator extends GraphqlSchemaBaseVisitor<Void> {
    private static final Map<String, SchemaScalarImpl> buildInScalars = new HashMap<>();
    private static final FieldInstantiator fieldInstantiator = new FieldInstantiator();

    static {
        buildInScalars.put("String", new SchemaScalarImpl(Collections.emptyMap(), "String", new Location(0, 0)));
        buildInScalars.put("Int", new SchemaScalarImpl(Collections.emptyMap(), "Int", new Location(0, 0)));
        buildInScalars.put("Boolean", new SchemaScalarImpl(Collections.emptyMap(), "Boolean", new Location(0, 0)));
        buildInScalars.put("Float", new SchemaScalarImpl(Collections.emptyMap(), "Float", new Location(0, 0)));
    }

    private final Map<String, SchemaScalarImpl> scalars = new HashMap<>();

    private final Map<String, SchemaInterfaceImpl> interfaces = new HashMap<>();

    private final Map<String, SchemaObjectTypeImpl> objectTypes = new HashMap<>();

    private final Map<String, SchemaInputImpl> inputs = new HashMap<>();

    public TypesInstantiator() {
        for (Map.Entry<String, SchemaScalarImpl> scalarEntry : buildInScalars.entrySet()) {
            scalars.put(scalarEntry.getKey(), scalarEntry.getValue());
        }
    }

    public Map<String, SchemaScalarImpl> getScalars() {
        return scalars;
    }

    public Map<String, SchemaInterfaceImpl> getInterfaces() {
        return interfaces;
    }

    public Map<String, SchemaObjectTypeImpl> getObjectTypes() {
        return objectTypes;
    }

    public Map<String, SchemaInputImpl> getInputs() {
        return inputs;
    }

    @Override
    public Void visitScalarDefinition(GraphqlSchemaParser.ScalarDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        String scalarName = nameToken.getText();

        SchemaScalarImpl existing = scalars.get(scalarName);

        if (existing != null) {
            if (buildInScalars.containsKey(scalarName)) {
                throw new SchemaParserException(
                        String.format("Overriding of built in scalar \"%s\" is not allowed at [%d:%d]",
                                scalarName, nameToken.getLine(), nameToken.getCharPositionInLine()));
            }

            throw new SchemaParserException(
                    String.format("Duplicate definition of scalar \"%s\" from [%d:%d] is not allowed at [%d:%d]",
                            scalarName, existing.getLocation().getLine(), existing.getLocation().getColumn(),
                            nameToken.getLine(), nameToken.getCharPositionInLine()));
        }

        SchemaScalarImpl scalar = new SchemaScalarImpl(buildAnnotations(ctx.annotation()), scalarName, new Location(nameToken));
        scalars.put(scalar.getName(), scalar);
        return null;
    }

    @Override
    public Void visitInterfaceDefinition(GraphqlSchemaParser.InterfaceDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        SchemaInterfaceImpl iface = new SchemaInterfaceImpl(buildAnnotations(ctx.annotation()), buildFields(ctx.fieldDefinition()), nameToken.getText(), new Location(nameToken));
        interfaces.put(iface.getName(), iface);
        return null;
    }

    @Override
    public Void visitTypeDefinition(GraphqlSchemaParser.TypeDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        List<String> interfaces;
        GraphqlSchemaParser.ImplementsListContext implementsListContext = ctx.implementsList();
        if (implementsListContext != null) {
            interfaces = implementsListContext.TYPE_NAME().stream()
                    .map(TerminalNode::getSymbol)
                    .map(Token::getText)
                    .collect(Collectors.toList());
        } else {
            interfaces = Collections.emptyList();
        }
        SchemaObjectTypeImpl objectType = new SchemaObjectTypeImpl(buildFields(ctx.fieldDefinition()),
                buildAnnotations(ctx.annotation()), interfaces, nameToken.getText(), new Location(nameToken));
        objectTypes.put(objectType.getName(), objectType);
        return null;
    }

    @Override
    public Void visitInputDefinition(GraphqlSchemaParser.InputDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        SchemaInputImpl input = new SchemaInputImpl(buildAnnotations(ctx.annotation()), buildFields(ctx.fieldDefinition()), nameToken.getText(), new Location(nameToken));
        inputs.put(input.getName(), input);
        return null;
    }

    private Map<String, SchemaFieldImpl> buildFields(List<GraphqlSchemaParser.FieldDefinitionContext> contexts) {
        try {
            return contexts.stream().map(context -> {
                Token fieldNameSymbol = context.FIELD_NAME().getSymbol();
                GraphqlSchemaParser.FieldArgumentsContext fieldArgumentsContext = context.fieldArguments();
                Map<String, SchemaFieldArgumentImpl> arguments;
                if (fieldArgumentsContext != null) {
                    arguments = fieldArgumentsContext.fieldArgument().stream().map(fac -> {
                        String argumentName = fac.FIELD_NAME().getSymbol().getText();
                        TypeReference typeReference = fieldInstantiator.visit(fac.fieldType());
                        return new SchemaFieldArgumentImpl(argumentName, typeReference);
                    }).collect(Collectors.toMap(SchemaFieldArgumentImpl::getArgumentName, Function.identity()));
                } else {
                    arguments = Collections.emptyMap();
                }

                return new SchemaFieldImpl(
                        fieldNameSymbol.getText(),
                        new Location(fieldNameSymbol),
                        fieldInstantiator.visitFieldDefinition(context),
                        arguments,
                        buildAnnotations(context.annotation()));
            }).collect(Collectors.toMap(SchemaFieldImpl::getName, Function.identity()));
        } catch (IllegalStateException ise) {
            throw new SchemaParserException("Failed to build fields", ise);
        }
    }

    private Map<String, SchemaAnnotationImpl> buildAnnotations(List<GraphqlSchemaParser.AnnotationContext> contexts) {
        return contexts.stream().map(context -> new SchemaAnnotationImpl(buildArguments(context.annotationArguments()), context.TYPE_NAME().getSymbol().getText(),
                new Location(context.ANNOTATION_START().getSymbol()))).collect(Collectors.toMap(SchemaAnnotationImpl::getName, Function.identity()));
    }

    private Map<String, Object> buildArguments(GraphqlSchemaParser.AnnotationArgumentsContext arguments) {
        if (arguments == null) {
            return Collections.emptyMap();
        }

        return arguments.annotationArgument().stream().collect(Collectors.toMap(argument -> argument.FIELD_NAME().getSymbol().getText(), argument -> buildValue(argument.value())));
    }

    private Object buildValue(GraphqlSchemaParser.ValueContext valueContext) {
        TerminalNode terminalNode;
        if ((terminalNode = valueContext.BOOLEAN_VALUE()) != null) {
            return Boolean.valueOf(terminalNode.getSymbol().getText());
        } else if ((terminalNode = valueContext.FLOAT_VALUE()) != null) {
            return Float.valueOf(terminalNode.getSymbol().getText());
        } else if ((terminalNode = valueContext.INT_VALUE()) != null) {
            return Integer.valueOf(terminalNode.getSymbol().getText());
        } else if ((terminalNode = valueContext.STRING_VALUE()) != null) {
            String text = terminalNode.getText();
            return text.substring(1, text.length() - 1); // Strip quotes
        }

        throw new SchemaParserException(
                String.format("Unknown value node at [%s]", new Location(valueContext.getStart())));
    }
}
