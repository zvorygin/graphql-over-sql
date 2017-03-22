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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);
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
                LOGGER.error("Unexpected token [{}] of type [{}] at position [{}:{}]. Expected any of [{}].",
                        offendingToken.getText(),
                        offendingDisplayName,
                        offendingToken.getLine(),
                        offendingToken.getCharPositionInLine(),
                        ime.getExpectedTokens().toString(vocabulary));
            }
            throw pce;
        }
    }
}
