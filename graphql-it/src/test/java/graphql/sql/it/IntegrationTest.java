package graphql.sql.it;

import graphql.sql.core.config.ConfigProvider;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.NameProvider;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.StaticDataFetcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-test-context.xml")
public class IntegrationTest {

    @Autowired
    private ConfigProvider configProvider;

    @Autowired
    private NameProvider nameProvider;

    @Test
    public void testConfigProvider() {
        GraphQLTypesProvider typesProvider = new GraphQLTypesProvider(configProvider.getConfig(), nameProvider);
        Collection<GraphQLInterfaceType> interfaceTypes = typesProvider.getInterfaceTypes();
        Collection<GraphQLObjectType> objectTypes = typesProvider.getObjectTypes();

        Set<GraphQLType> dictionary = new HashSet<>();
        dictionary.addAll(interfaceTypes);
        dictionary.addAll(objectTypes);

        List<GraphQLFieldDefinition> fields = new ArrayList<>();

        for (GraphQLInterfaceType interfaceType : interfaceTypes) {
            String typeName = interfaceType.getName().substring(1);
            fields.add(new GraphQLFieldDefinition(
                    typeName,
                    "query for " + typeName,
                    interfaceType, new StaticDataFetcher(null),
                    Collections.emptyList(),
                    null));
        }

        GraphQLObjectType queryType = new GraphQLObjectType("query", "Query Object", fields, Collections.emptyList());
        GraphQLSchema schema = new GraphQLSchema(queryType, null, dictionary);
    }
}
