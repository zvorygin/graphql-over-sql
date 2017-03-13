package graphql.sql.core.config.domain;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class Link {
    @Nonnull
    private final String sourceEntity;

    @Nonnull
    private final String targetEntity;

    @Nonnull
    private final List<String> sourceFields;

    @Nonnull
    private final List<String> targetFields;

    @Nonnull
    private final String linkName;

    public Link(@Nonnull String sourceEntity,
                @Nonnull String targetEntity,
                @Nonnull List<String> sourceFields,
                @Nonnull List<String> targetFields,
                @Nonnull String linkName) {
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.sourceFields = sourceFields;
        this.targetFields = targetFields;
        this.linkName = linkName;
    }

    @Nonnull
    public String getSourceEntity() {
        return sourceEntity;
    }

    @Nonnull
    public String getTargetEntity() {
        return targetEntity;
    }

    @Nonnull
    public List<String> getSourceFields() {
        return sourceFields;
    }

    @Nonnull
    public List<String> getTargetFields() {
        return targetFields;
    }

    @Nonnull
    public String getLinkName() {
        return linkName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        return Objects.equals(sourceEntity, link.sourceEntity) &&
                Objects.equals(targetEntity, link.targetEntity) &&
                Objects.equals(linkName, link.linkName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceEntity, targetEntity, linkName);
    }
}
