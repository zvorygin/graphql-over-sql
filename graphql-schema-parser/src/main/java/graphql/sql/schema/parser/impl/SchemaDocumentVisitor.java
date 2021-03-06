package graphql.sql.schema.parser.impl;

import graphql.sql.schema.antlr.GraphqlSchemaBaseVisitor;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import graphql.sql.schema.parser.SchemaDocument;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class SchemaDocumentVisitor extends GraphqlSchemaBaseVisitor<SchemaDocument> {

    @Override
    public SchemaDocument visitSchemaDocument(GraphqlSchemaParser.SchemaDocumentContext ctx) {
        TypesInstantiator typesVisitor = new TypesInstantiator();
        ctx.definition().forEach(typesVisitor::visitDefinition);
        Map<String, SchemaObjectTypeImpl> objectTypes = typesVisitor.getObjectTypes();
        Map<String, SchemaInterfaceImpl> interfaces = typesVisitor.getInterfaces();
        Map<String, SchemaScalarImpl> scalars = typesVisitor.getScalars();

        Map<String, SchemaInputImpl> inputs = typesVisitor.getInputs();
        Token queryTypeNameSymbol = ctx.schema().query().TYPE_NAME().getSymbol();
        String queryTypeName = queryTypeNameSymbol.getText();
        TypeReferenceImpl queryType =  new TypeReferenceImpl(queryTypeName, new Location(queryTypeNameSymbol));
        TypeReferenceImpl mutationType;
        GraphqlSchemaParser.MutationContext mutation = ctx.schema().mutation();
        if (mutation != null) {
            Token mutationTypeNameSymbol = mutation.TYPE_NAME().getSymbol();
            String mutationTypeName = mutationTypeNameSymbol.getText();
            mutationType = new TypeReferenceImpl(mutationTypeName, new Location(mutationTypeNameSymbol));
        } else {
            mutationType = null;
        }
        SchemaImpl schema = new SchemaImpl(queryType, mutationType, new Location(ctx.schema().SCHEMA().getSymbol()));
        return new SchemaDocumentImpl(schema, objectTypes, interfaces, scalars, inputs);
    }
}
