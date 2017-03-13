package graphql.sql.core.config.domain;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;

import javax.annotation.Nonnull;

public class EntityReference {
    @Nonnull
    private final String name;

    @Nonnull
    private final DbJoin join;

    @Nonnull
    private final DbJoin reverseJoin;

    @Nonnull
    private final Entity targetEntity;

    @Nonnull
    private final ReferenceType referenceType;

    private final boolean nullable;

    public EntityReference(@Nonnull String name,
                           @Nonnull DbJoin join,
                           @Nonnull DbJoin reverseJoin,
                           @Nonnull Entity targetEntity,
                           @Nonnull ReferenceType referenceType,
                           boolean nullable) {
        this.name = name;
        this.join = join;
        this.reverseJoin = reverseJoin;
        this.targetEntity = targetEntity;
        this.referenceType = referenceType;
        this.nullable = nullable;
        if (!join.getToTable().equals(targetEntity.getTable())) {
            throw new IllegalStateException(String.format(
                    "Join [%s] doesn't point target table [%s] but instead points from [%s] to [%s]",
                    join.getName(),
                    targetEntity.getTable().getAbsoluteName(),
                    join.getFromTable().getAbsoluteName(),
                    join.getToTable().getAbsoluteName()));
        }
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public DbJoin getJoin() {
        return join;
    }

    @Nonnull
    public DbJoin getReverseJoin() {
        return reverseJoin;
    }

    @Nonnull
    public Entity getTargetEntity() {
        return targetEntity;
    }

    @Nonnull
    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public boolean isNullable() {
        return nullable;
    }
}
