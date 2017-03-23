package graphql.sql.schema.parser.impl;

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
    private static final Map<String, ScalarImpl> buildInScalars = new HashMap<>();
    private static final FieldInstantiator fieldInstantiator = new FieldInstantiator();

    static {
        buildInScalars.put("String", new ScalarImpl(Collections.emptyMap(), "String", new Location(0, 0)));
        buildInScalars.put("Int", new ScalarImpl(Collections.emptyMap(), "Int", new Location(0, 0)));
        buildInScalars.put("Boolean", new ScalarImpl(Collections.emptyMap(), "Boolean", new Location(0, 0)));
        buildInScalars.put("Float", new ScalarImpl(Collections.emptyMap(), "Float", new Location(0, 0)));
    }

    private final Map<String, ScalarImpl> scalars = new HashMap<>();

    private final Map<String, InterfaceImpl> interfaces = new HashMap<>();

    private final Map<String, ObjectTypeImpl> objectTypes = new HashMap<>();

    private final Map<String, InputImpl> inputs = new HashMap<>();

    public TypesInstantiator() {
        for (Map.Entry<String, ScalarImpl> scalarEntry : buildInScalars.entrySet()) {
            scalars.put(scalarEntry.getKey(), scalarEntry.getValue());
        }
    }

    public Map<String, ScalarImpl> getScalars() {
        return scalars;
    }

    public Map<String, InterfaceImpl> getInterfaces() {
        return interfaces;
    }

    public Map<String, ObjectTypeImpl> getObjectTypes() {
        return objectTypes;
    }

    public Map<String, InputImpl> getInputs() {
        return inputs;
    }

    @Override
    public Void visitScalarDefinition(GraphqlSchemaParser.ScalarDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        String scalarName = nameToken.getText();

        ScalarImpl existing = scalars.get(scalarName);

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

        ScalarImpl scalar = new ScalarImpl(buildAnnotations(ctx.annotation()), scalarName, new Location(nameToken));
        scalars.put(scalar.getName(), scalar);
        return null;
    }

    @Override
    public Void visitInterfaceDefinition(GraphqlSchemaParser.InterfaceDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        InterfaceImpl iface = new InterfaceImpl(buildAnnotations(ctx.annotation()), buildFields(ctx.fieldDefinition()), nameToken.getText(), new Location(nameToken));
        interfaces.put(iface.getName(), iface);
        return null;
    }

    @Override
    public Void visitTypeDefinition(GraphqlSchemaParser.TypeDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        ObjectTypeImpl objectType = new ObjectTypeImpl(buildFields(ctx.fieldDefinition()), buildAnnotations(ctx.annotation()), nameToken.getText(), new Location(nameToken));
        objectTypes.put(objectType.getName(), objectType);
        return null;
    }

    @Override
    public Void visitInputDefinition(GraphqlSchemaParser.InputDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        InputImpl input = new InputImpl(buildAnnotations(ctx.annotation()), buildFields(ctx.fieldDefinition()), nameToken.getText(), new Location(nameToken));
        inputs.put(input.getName(), input);
        return null;
    }

    private Map<String, FieldImpl> buildFields(List<GraphqlSchemaParser.FieldDefinitionContext> contexts) {
        try {
            return contexts.stream().map(context -> {
                Token fieldNameSymbol = context.FIELD_NAME().getSymbol();
                return new FieldImpl(
                        fieldNameSymbol.getText(),
                        new Location(fieldNameSymbol),
                        fieldInstantiator.visitFieldDefinition(context));
            }).collect(Collectors.toMap(FieldImpl::getName, Function.identity()));
        } catch (IllegalStateException ise) {
            throw new SchemaParserException("Failed to build fields", ise);
        }
    }

    private Map<String, AnnotationImpl> buildAnnotations(List<GraphqlSchemaParser.AnnotationContext> contexts) {
        return contexts.stream().map(context -> new AnnotationImpl(buildArguments(context.annotationArguments()), context.TYPE_NAME().getSymbol().getText(),
                new Location(context.ANNOTATION_START().getSymbol()))).collect(Collectors.toMap(AnnotationImpl::getName, Function.identity()));
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
