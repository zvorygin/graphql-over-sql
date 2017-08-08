package graphql.sql.core.config.domain;

import com.google.common.collect.Sets;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;
import graphql.sql.core.config.Scalar;
import graphql.sql.core.config.TypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public interface Config {
    Map<String, Interface> getInterfaces();

    Map<String, ObjectType> getTypes();

    Map<String, Scalar> getScalars();

    String getQueryTypeName();

    default CompositeType getType(String typeName) {
        Map<String, Interface> interfaces = getInterfaces();
        Interface iface = interfaces.get(typeName);
        if (iface != null) {
            return iface;
        }

        return getTypes().get(typeName);
    }

    default CompositeType getType(TypeReference typeReference) {
        return getType(typeReference.getTypeName());
    }

    default Scalar getScalar(TypeReference typeReference) {
        return getScalars().get(typeReference.getTypeName());
    }


    default Collection<CompositeType> getJoinableTypes(CompositeType type) {
        //TODO(dzvorygin) introduce some sort of caching here.

        Collection<CompositeType> result = new ArrayList<>();
        addJoinableInterfaces(type, type.getInterfaces(), result);

        for (ObjectType objectType : getTypes().values()) {
            Collection<Interface> interfaces = objectType.getInterfaces();
            if (interfaces.contains(type)) {
                addJoinableInterfaces(type, interfaces, result);
                if (isJoinable(type, objectType)) {
                    result.add(objectType);
                }
            }
        }

        return result;
    }

    static void addJoinableInterfaces(CompositeType type,
                                      Collection<Interface> interfaces,
                                      Collection<CompositeType> result) {
        for (Interface anInterface : interfaces) {
            if (isJoinable(type, anInterface)) {
                result.add(anInterface);
            }
        }
    }

    static boolean isJoinable(CompositeType typeA, CompositeType typeB) {
        return typeB != typeA && !Sets.intersection(typeB.getFields().keySet(),
                typeA.getFields().keySet()).isEmpty();
    }
}
