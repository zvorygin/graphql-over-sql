package graphql.sql.core.extractor;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ArrayKey {
    private final Object[] value;
    private final int hashCode;

    @Nullable
    public static ArrayKey createArrayKey(Object[] value) {
        for (Object o : value) {
            if (o != null) {
                return new ArrayKey(value);
            }
        }

        return null;
    }

    private ArrayKey(Object[] value) {
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

        ArrayKey arrayKey = (ArrayKey) o;

        return hashCode == arrayKey.hashCode && Arrays.equals(value, arrayKey.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public Object at(int index) {
        return value[index];
    }

    @Override
    public String toString() {
        return "ArrayKey{" +
                "value=" + Arrays.toString(value) +
                '}';
    }
}
