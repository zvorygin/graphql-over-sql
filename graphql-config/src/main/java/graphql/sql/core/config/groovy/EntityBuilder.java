package graphql.sql.core.config.groovy;

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.ReferenceType;
import graphql.sql.core.config.domain.impl.SqlEntityReference;
import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.config.groovy.context.ExecutionContext;
import graphql.sql.core.config.groovy.context.GroovyEntityBuilder;
import graphql.sql.core.introspect.DatabaseIntrospector;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class EntityBuilder {

    private final ExecutionContext executionContext;

    public EntityBuilder(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @SuppressWarnings("unused")
    public Entity call(Map<String, Object> parameters) {
        GroovyEntityBuilder delegate =
                new GroovyEntityBuilder(executionContext.getSchemaName());
        String name = (String) parameters.get("name");
        if (name != null) {
            delegate.name(name);
        }
        String schemaName = (String) parameters.get("schema");
        if (schemaName != null) {
            delegate.schema(schemaName);
        }
        delegate.table((String) parameters.get("table"));
        delegate.parent((SqlEntity) parameters.get("parent"));
        DatabaseIntrospector introspector = executionContext.getIntrospector();
        SqlEntity entity = delegate.build(introspector, executionContext.getNameProvider());

        executionContext.registerEntity(entity);

        introspector.getForeignKeyConstraints(entity.getTable())
                .forEach(constraint -> processForeignKeyConstraint(entity, constraint));

        return entity;
    }

    private void processForeignKeyConstraint(SqlEntity entity, DbForeignKeyConstraint constraint) {
        DatabaseIntrospector introspector = executionContext.getIntrospector();
        NameProvider nameProvider = executionContext.getNameProvider();
        executionContext.findEntity(constraint.getReferencedTable()).ifPresent(referencedEntity ->
                {
                    //TODO(dzvorygin) remove cast below
                    Optional<SqlEntityReference> parentReference = Optional.ofNullable(entity.getParentReference());

                    // Don't register parent/child references in field references
                    DbJoin join = introspector.getJoin(constraint);
                    if (parentReference.map(r -> r.getJoin().equals(join)).orElse(false)) {
                        return;
                    }

                    DbJoin reverseJoin = introspector.getReverseJoin(constraint);
                    // If any column is missing NOT_NULL constraint, then this reference is nullable
                    boolean nullable = join.getFromColumns().stream().map(
                            dbColumn -> dbColumn.getConstraints().stream().map(DbConstraint::getType)
                                    .anyMatch(Predicate.isEqual(Constraint.Type.NOT_NULL))
                    ).anyMatch(Predicate.isEqual(false));

                    entity.addReference(
                            new SqlEntityReference(nameProvider.getLinkName(constraint,
                                    entity, referencedEntity, ReferenceType.MANY_TO_ONE),
                                    join,
                                    referencedEntity,
                                    ReferenceType.MANY_TO_ONE,
                                    nullable
                            ));
                    referencedEntity.addReference(
                            new SqlEntityReference(nameProvider.getLinkName(
                                    constraint, referencedEntity, entity, ReferenceType.ONE_TO_MANY),
                                    reverseJoin,
                                    entity,
                                    ReferenceType.ONE_TO_MANY,
                                    false)
                    );
                }
        );
    }
}
