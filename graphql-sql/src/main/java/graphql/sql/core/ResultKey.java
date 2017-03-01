package graphql.sql.core;

import java.util.Arrays;

public class ResultKey {

    private final int hashCode;

    private final Object[] value;

    public ResultKey(Object[] value) {
        this.value = value;
        hashCode = Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResultKey resultKey = (ResultKey) o;
        return hashCode == resultKey.hashCode &&
                Arrays.equals(value, resultKey.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
