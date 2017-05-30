package graphql.sql.engine;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import graphql.Directives;
import graphql.GraphQLException;
import graphql.language.*;

import javax.annotation.Nonnull;
import java.util.*;

public class DocumentContext {
    private final Map<String, FragmentDefinition> fragmentsByName = new LinkedHashMap<>();
    private final Map<String, OperationDefinition> operationsByName = new LinkedHashMap<>();
    private final Map<OperationDefinition, Set<String>> queryAffectingFlags = new LinkedHashMap<>();

    public DocumentContext(Document document) {
        for (Definition definition : document.getDefinitions()) {
            if (definition instanceof OperationDefinition) {
                OperationDefinition operationDefinition = (OperationDefinition) definition;
                operationsByName.put(operationDefinition.getName(), operationDefinition);
            }
            if (definition instanceof FragmentDefinition) {
                FragmentDefinition fragmentDefinition = (FragmentDefinition) definition;
                fragmentsByName.put(fragmentDefinition.getName(), fragmentDefinition);
            }
        }

        for (OperationDefinition operationDefinition : operationsByName.values()) {
            HashSet<String> flags = new HashSet<>();
            buildQueryAffectingFlags(operationDefinition.getSelectionSet(), flags);
            buildQueryAffectingFlags(operationDefinition.getDirectives(), flags);
            queryAffectingFlags.put(operationDefinition, flags);
        }
    }


    @Nonnull
    public OperationDefinition getOperation(String operationName) {
        if (operationName == null) {
            if (operationsByName.size() > 1) {
                throw new GraphQLException(String.format(
                        "Document contains multiple operation names [%s], and no operation name was provided",
                        Joiner.on(',').join(operationsByName.keySet())));
            }
            return Iterables.getOnlyElement(operationsByName.values());
        }

        OperationDefinition result = operationsByName.get(operationName);

        if (result == null) {
            throw new GraphQLException(String.format(
                    "Operation with name [%s] wasn't found among operations [%s]",
                    operationName,
                    Joiner.on(',').join(operationsByName.keySet())));
        }

        return result;
    }

    @Nonnull
    public Set<String> getQueryAffectingFlags(OperationDefinition operationDefinition) {
        Set<String> result = queryAffectingFlags.get(operationDefinition);
        if (result == null) {
            throw new IllegalStateException(
                    String.format("Failed to find queryAffectingFlags for operation [%s]",
                            operationDefinition.getName()));
        }
        return result;
    }

    /**
     * Recursive search to find all variables referenced in @skip, or in @include directives
     *
     * @param selectionSet fields to scan
     * @param flags        set to use as output
     */
    private void buildQueryAffectingFlags(SelectionSet selectionSet, Set<String> flags) {
        for (Selection selection : selectionSet.getSelections()) {
            if (selection instanceof Field) {
                buildQueryAffectingFlags(((Field) selection).getDirectives(), flags);
            } else if (selection instanceof FragmentSpread) {
                FragmentSpread fragmentSpread = (FragmentSpread) selection;
                buildQueryAffectingFlags(fragmentSpread.getDirectives(), flags);
                FragmentDefinition referencedFragment = fragmentsByName.get(fragmentSpread.getName());
                buildQueryAffectingFlags(referencedFragment.getSelectionSet(), flags);
            } else if (selection instanceof InlineFragment) {
                InlineFragment inlineFragment = (InlineFragment) selection;
                buildQueryAffectingFlags(inlineFragment.getDirectives(), flags);
                buildQueryAffectingFlags(inlineFragment.getSelectionSet(), flags);
            }
        }
    }

    private void buildQueryAffectingFlags(List<Directive> directives, Set<String> flags) {
        for (Directive directive : directives) {
            String argumentName = directive.getName();
            if (argumentName.equals(Directives.IncludeDirective.getName()) ||
                    argumentName.equals(Directives.SkipDirective.getName())) {
                List<Argument> arguments = directive.getArguments();
                Value value = Iterables.getOnlyElement(arguments).getValue();
                if (value instanceof VariableReference) {
                    flags.add(((VariableReference) value).getName());
                }
            }
        }
    }

    public Map<String, FragmentDefinition> getFragmentsByName() {
        return fragmentsByName;
    }
}
