package graphql.sql.schema.impl;

import graphql.sql.schema.antlr.GraphqlSchemaBaseVisitor;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import org.antlr.v4.runtime.Token;

import java.util.Map;

class TypeFieldsInstantiator extends GraphqlSchemaBaseVisitor<Void> {
    private final TypesInstantiator typesInstantiator;
    private final FieldInstantiator fieldInstantiator;

    public TypeFieldsInstantiator(TypesInstantiator typesInstantiator) {
        this.typesInstantiator = typesInstantiator;
        fieldInstantiator = new FieldInstantiator();
    }

    @Override
    public Void visitInterfaceDefinition(GraphqlSchemaParser.InterfaceDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        Map<String, InterfaceImpl> interfaces = typesInstantiator.getInterfaces();
        InterfaceImpl iface = interfaces.get(nameToken.getText());
        ctx.fieldDefinition().stream().map(this::buildField).forEach(iface::addField);
        return null;
    }

    @Override
    public Void visitTypeDefinition(GraphqlSchemaParser.TypeDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        Map<String, ObjectTypeImpl> objectTypes = typesInstantiator.getObjectTypes();
        ObjectTypeImpl objectType = objectTypes.get(nameToken.getText());
        ctx.fieldDefinition().stream().map(this::buildField).forEach(objectType::addField);
        return null;
    }

    @Override
    public Void visitInputDefinition(GraphqlSchemaParser.InputDefinitionContext ctx) {
        Token nameToken = ctx.TYPE_NAME().getSymbol();
        Map<String, InputImpl> inputs = typesInstantiator.getInputs();
        InputImpl input = inputs.get(nameToken.getText());
        ctx.fieldDefinition().stream().map(this::buildField).forEach(input::addField);
        return null;
    }


    private FieldImpl buildField(GraphqlSchemaParser.FieldDefinitionContext fieldDefinitionContext) {
        Token fieldNameSymbol = fieldDefinitionContext.FIELD_NAME().getSymbol();
        return new FieldImpl(fieldNameSymbol.getText(), new Location(fieldNameSymbol),
                fieldInstantiator.visitFieldDefinition(fieldDefinitionContext));
    }
}
