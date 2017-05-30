package graphql.sql.core.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

public class BfsFinder {
    public static <R, T extends R> List<T> findPath(R root, T needle, Function<R, Iterable<? extends T>> childrenFunction) {
        Set<T> processed = new HashSet<>();

        Queue<LinkedNode<T>> queue = new ArrayDeque<>();

        childrenFunction.apply(root).forEach(i -> queue.add(new LinkedNode<>(i, null)));

        while (!queue.isEmpty()) {
            LinkedNode<T> head = queue.poll();
            if (head.item.equals(needle)) {
                ArrayList<T> result = new ArrayList<>();
                while (head != null) {
                    result.add(head.item);
                    head = head.parent;
                }
                Collections.reverse(result);
                return result;
            }
            processed.add(head.item);
            Iterable<? extends T> children = childrenFunction.apply(head.item);
            for (T c : children) {
                if (!processed.contains(c)) {
                    queue.add(new LinkedNode<>(c, head));
                }
            }
        }

        return null;
    }

    private static final class LinkedNode<T> {
        final T item;
        final LinkedNode<T> parent;

        public LinkedNode(T item, LinkedNode<T> parent) {
            this.item = item;
            this.parent = parent;
        }
    }
}
