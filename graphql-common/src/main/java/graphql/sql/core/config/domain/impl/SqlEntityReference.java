package graphql.sql.core.config.domain.impl;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.config.domain.ReferenceType;

import javax.annotation.Nonnull;

public class SqlEntityReference implements EntityReference {
    @Nonnull
    private final String name;

    @Nonnull
    private final DbJoin join;

    @Nonnull
    private final SqlEntity targetEntity;

    @Nonnull
    private final ReferenceType referenceType;

    private final boolean nullable;

    @Nonnull
    private final String description;

    public SqlEntityReference(@Nonnull String name,
                              @Nonnull DbJoin join,
                              @Nonnull SqlEntity targetEntity,
                              @Nonnull ReferenceType referenceType,
                              boolean nullable) {
        if (!join.getToTable().equals(targetEntity.getTable())) {
            throw new IllegalStateException(String.format(
                    "Join [%s] doesn't point target table [%s] but instead points from [%s] to [%s]",
                    join.getName(),
                    targetEntity.getTable().getAbsoluteName(),
                    join.getFromTable().getAbsoluteName(),
                    join.getToTable().getAbsoluteName()));
        }

        this.name = name;
        this.join = join;
        this.targetEntity = targetEntity;
        this.referenceType = referenceType;
        this.nullable = nullable;
        description = String.format("Reference [%s] from to entity [%s]", name, targetEntity.getEntityName());
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public DbJoin getJoin() {
        return join;
    }

    @Override
    @Nonnull
    public SqlEntity getTargetEntity() {
        return targetEntity;
    }

    @Override
    @Nonnull
    public ReferenceType getReferenceType() {
        return referenceType;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return description;
    }
}
