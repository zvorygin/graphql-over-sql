package graphql.sql.core.config.impl;

import com.google.common.base.Splitter;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;

import graphql.sql.core.config.AbbreviationResolver;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.ReferenceType;

public class UnderscoreToCamelcaseNameProvider implements NameProvider {

    private final AbbreviationResolver abbreviationResolver;

    public UnderscoreToCamelcaseNameProvider(AbbreviationResolver abbreviationResolver) {
        this.abbreviationResolver = abbreviationResolver;
    }

    @Override
    public String getEntityName(String tableName) {
        String camelCased = underscoreToCamelCase(tableName);
        return uppercaseFirst(camelCased);
    }

    private String uppercaseFirst(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String lowercaseFirst(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public String getFieldName(String columnName) {
        return underscoreToCamelCase(columnName);
    }

    @Override
    public String getLinkName(DbForeignKeyConstraint constraint, Entity from, Entity to, ReferenceType type) {
        return lowercaseFirst(type.equals(ReferenceType.MANY_TO_ONE) ? to.getEntityName() : to.getEntityName() + "s");
    }

    @Override
    public String getFieldListName(EntityField entityField) {
        return entityField.getFieldName() + "s";
    }

    private String underscoreToCamelCase(String term) {
        StringBuilder result = new StringBuilder();
        for (String segment : Splitter.on('_').omitEmptyStrings().split(term)) {
            Iterable<String> resolved = abbreviationResolver.resolve(segment);
            for (String word : resolved) {
                if (result.length() == 0) {
                    result.append(word.toLowerCase());
                } else {
                    result.append(Character.toUpperCase(word.charAt(0))).append(word.toLowerCase(), 1, word.length());
                }
            }
        }
        return result.toString();
    }

}
