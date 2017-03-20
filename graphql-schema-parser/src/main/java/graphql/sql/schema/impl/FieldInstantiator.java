package graphql.sql.schema.impl;

import graphql.sql.schema.TypeReference;
import graphql.sql.schema.antlr.GraphqlSchemaBaseVisitor;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import org.antlr.v4.runtime.Token;

class FieldInstantiator extends GraphqlSchemaBaseVisitor<TypeReference> {
    @Override
    public TypeReference visitFieldTypeNameReference(GraphqlSchemaParser.FieldTypeNameReferenceContext ctx) {
        Token symbol = ctx.TYPE_NAME().getSymbol();
        return new TypeReferenceImpl(symbol.getText(), new Location(symbol));
    }

    @Override
    public TypeReference visitFieldTypeList(GraphqlSchemaParser.FieldTypeListContext ctx) {
        return new ListType(ctx.fieldType().accept(this), new Location(ctx.LBRACKET().getSymbol()));
    }

    @Override
    public TypeReference visitFieldTypeNotNull(GraphqlSchemaParser.FieldTypeNotNullContext ctx) {
        return new NotNullType(ctx.fieldType().accept(this), new Location(ctx.EXCLM().getSymbol()));
    }
}
