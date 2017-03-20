package graphql.sql.schema;

import graphql.sql.schema.antlr.GraphqlSchemaLexer;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import graphql.sql.schema.impl.SchemaDocumentVisitor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;

public class Parser {
    private final SchemaDocumentVisitor visitor = new SchemaDocumentVisitor();

    public SchemaDocument parse(InputStream inputStream) throws IOException {
        GraphqlSchemaLexer lexer = new GraphqlSchemaLexer(new ANTLRInputStream(inputStream));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        GraphqlSchemaParser impl = new GraphqlSchemaParser(tokenStream);
        impl.setErrorHandler(new BailErrorStrategy());
        return visitor.visit(impl.schemaDocument());
    }
}
