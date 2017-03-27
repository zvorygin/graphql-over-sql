package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaTypeReference;
import graphql.sql.schema.antlr.GraphqlSchemaBaseVisitor;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import org.antlr.v4.runtime.Token;

class FieldInstantiator extends GraphqlSchemaBaseVisitor<SchemaTypeReference> {
    @Override
    public SchemaTypeReference visitFieldTypeNameReference(GraphqlSchemaParser.FieldTypeNameReferenceContext ctx) {
        Token symbol = ctx.TYPE_NAME().getSymbol();
        return new TypeReferenceImpl(symbol.getText(), new Location(symbol));
    }

    @Override
    public SchemaTypeReference visitFieldTypeList(GraphqlSchemaParser.FieldTypeListContext ctx) {
        return new ListSchemaTypeReference(ctx.fieldType().accept(this), new Location(ctx.LBRACKET().getSymbol()));
    }

    @Override
    public SchemaTypeReference visitFieldTypeNotNull(GraphqlSchemaParser.FieldTypeNotNullContext ctx) {
        return new NotNullSchemaTypeReference(ctx.fieldType().accept(this), new Location(ctx.EXCLM().getSymbol()));
    }
}
