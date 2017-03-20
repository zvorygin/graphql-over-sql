package graphql.sql.schema.impl;

import graphql.sql.schema.SchemaParserException;
import graphql.sql.schema.antlr.GraphqlSchemaBaseVisitor;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.Map;

class TypesInstantiator extends GraphqlSchemaBaseVisitor<Void> {
    private static final Map<String, ScalarImpl> buildInScalars = new HashMap<>();

    static {
        buildInScalars.put("String", new ScalarImpl("String", new Location(0, 0)));
        buildInScalars.put("Int", new ScalarImpl("Int", new Location(0, 0)));
        buildInScalars.put("Boolean", new ScalarImpl("Boolean", new Location(0, 0)));
        buildInScalars.put("Float", new ScalarImpl("Float", new Location(0, 0)));
    }

    private final Map<String, NamedNode> existingNodes = new HashMap<>();

    private final Map<String, ScalarImpl> scalars = new HashMap<>();

    private final Map<String, InterfaceImpl> interfaces = new HashMap<>();

    private final Map<String, ObjectTypeImpl> objectTypes = new HashMap<>();

    private final Map<String, InputImpl> inputs = new HashMap<>();

    public TypesInstantiator() {
        for (Map.Entry<String, ScalarImpl> scalarEntry : buildInScalars.entrySet()) {
            scalars.put(scalarEntry.getKey(), scalarEntry.getValue());
        }
    }

    public Map<String, NamedNode> getExistingNodes() {
        return existingNodes;
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

        ScalarImpl scalar = new ScalarImpl(scalarName, new Location(nameToken));
        scalars.put(scalar.getName(), registerType(scalar));
        return null;
    }

    @Override
    public Void visitInterfaceDefinition(GraphqlSchemaParser.InterfaceDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        InterfaceImpl iface = new InterfaceImpl(nameToken.getText(), new Location(nameToken));
        interfaces.put(iface.getName(), registerType(iface));
        return null;
    }

    @Override
    public Void visitTypeDefinition(GraphqlSchemaParser.TypeDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        ObjectTypeImpl objectType = new ObjectTypeImpl(nameToken.getText(), new Location(nameToken));
        objectTypes.put(objectType.getName(), registerType(objectType));
        return null;
    }

    @Override
    public Void visitInputDefinition(GraphqlSchemaParser.InputDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        InputImpl input = new InputImpl(nameToken.getText(), new Location(nameToken));
        inputs.put(input.getName(), registerType(input));
        return null;
    }

    private <T extends NamedNode> T registerType(T type) {
        existingNodes.put(type.getName(), type);
        return type;
    }
}
