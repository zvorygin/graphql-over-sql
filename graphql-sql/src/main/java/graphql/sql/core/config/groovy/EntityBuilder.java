package graphql.sql.core.config.groovy;

import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.config.domain.ReferenceType;
import graphql.sql.core.config.groovy.context.ExecutionContext;
import graphql.sql.core.config.groovy.context.GroovyEntityBuilder;
import graphql.sql.core.introspect.DatabaseIntrospector;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;

import java.util.Map;
import java.util.Optional;

public class EntityBuilder {

    private final ExecutionContext executionContext;

    public EntityBuilder(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @SuppressWarnings("unused")
    public Entity call(Map<String, Object> parameters) {
        GroovyEntityBuilder delegate =
                new GroovyEntityBuilder(executionContext.getCatalogName(), executionContext.getSchemaName());
        String name = (String) parameters.get("name");
        if (name != null) {
            delegate.name(name);
        }
        String schemaName = (String) parameters.get("schema");
        if (schemaName != null) {
            delegate.schema(schemaName);
        }
        delegate.table((String) parameters.get("table"));
        delegate.parent((Entity) parameters.get("parent"));
        DatabaseIntrospector introspector = executionContext.getIntrospector();
        Entity entity = delegate.build(introspector, executionContext.getNameProvider());

        executionContext.registerEntity(entity);

        introspector.getForeignKeyConstraints(entity.getTable())
                .forEach(constraint -> processForeignKeyConstraint(entity, constraint));

        return entity;
    }

    private void processForeignKeyConstraint(Entity entity, DbForeignKeyConstraint constraint) {
        DatabaseIntrospector introspector = executionContext.getIntrospector();
        NameProvider nameProvider = executionContext.getNameProvider();
        executionContext.findEntity(constraint.getReferencedTable()).ifPresent(referencedEntity -> {
            Optional<EntityReference> parentReference = Optional.ofNullable(entity.getParentReference());

                    // Don't register parent/child references in field references
                    DbJoin join = introspector.getJoin(constraint);
                    if (parentReference.map(r -> r.getJoin().equals(join)).orElse(false)) {
                        return;
                    }

                    DbJoin reverseJoin = introspector.getReverseJoin(constraint);
                    entity.addReference(
                            new EntityReference(nameProvider.getLinkName(constraint,
                                    entity, referencedEntity, ReferenceType.MANY_TO_ONE),
                                    join,
                                    reverseJoin,
                                    referencedEntity,
                                    ReferenceType.MANY_TO_ONE));
                    referencedEntity.addReference(
                            new EntityReference(nameProvider.getLinkName(
                                    constraint, referencedEntity, entity, ReferenceType.ONE_TO_MANY),
                                    reverseJoin,
                                    join,
                                    entity,
                                    ReferenceType.ONE_TO_MANY)
                    );
                }
        );
    }
}
