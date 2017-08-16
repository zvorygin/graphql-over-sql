package graphql.sql.schema.parser;

import graphql.sql.schema.antlr.GraphqlSchemaLexer;
import graphql.sql.schema.antlr.GraphqlSchemaParser;
import graphql.sql.schema.parser.impl.SchemaDocumentVisitor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;
import java.io.InputStream;

public class SchemaParser {
    private final SchemaDocumentVisitor visitor = new SchemaDocumentVisitor();

    public SchemaDocument parse(InputStream inputStream) throws IOException {
        try {
            GraphqlSchemaLexer lexer = new GraphqlSchemaLexer(new ANTLRInputStream(inputStream));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            GraphqlSchemaParser impl = new GraphqlSchemaParser(tokenStream);
            impl.setErrorHandler(new BailErrorStrategy());
            return visitor.visit(impl.schemaDocument());
        } catch (ParseCancellationException pce) {
            Throwable cause = pce.getCause();
            if (cause instanceof InputMismatchException) {
                InputMismatchException ime = (InputMismatchException) cause;
                Vocabulary vocabulary = ime.getRecognizer().getVocabulary();
                Token offendingToken = ime.getOffendingToken();
                String offendingDisplayName = vocabulary.getDisplayName(offendingToken.getType());
                String message = String.format("Unexpected token [%s] of type [%s] at position [%d:%d]. Expected any of [%s].",
                        offendingToken.getText(),
                        offendingDisplayName,
                        offendingToken.getLine(),
                        offendingToken.getCharPositionInLine(),
                        ime.getExpectedTokens().toString(vocabulary));
                throw new SchemaParserException(message, pce);
            }
            throw pce;
        }
    }
}
