package graphql.sql.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

public class TopologicalIterator<T> implements Iterator<T> {

    private final Iterator<T> impl;

    public TopologicalIterator(Collection<? extends T> elements, Function<T, Iterable<T>> childrenFunction) {
        LinkedHashSet<T> result = new LinkedHashSet<>();

        Stack<Node<T>> stack = new Stack<>();

        for (T element : elements) {
            if (result.contains(element)) {
                continue;
            }

            Set<T> onStack = new HashSet<>();
            onStack.add(element);
            stack.push(new Node<>(element, childrenFunction.apply(element).iterator()));
            while (!stack.empty()) {
                Node<T> top = stack.peek();
                if (top.childIterator.hasNext()) {
                    T child = top.childIterator.next();
                    if (!result.contains(child)) {
                        if (!onStack.add(child)) {
                            List<T> loop = new ArrayList<>();
                            loop.add(child);
                            while (!stack.peek().item.equals(child)) {
                                loop.add(stack.pop().item);
                            }
                            throw new LoopException(loop);
                        }
                        stack.push(new Node<>(child, childrenFunction.apply(child).iterator()));
                    }
                } else {
                    stack.pop();
                    onStack.remove(top.item);
                    result.add(top.item);
                }
            }
        }

        impl = result.iterator();
    }

    @Override
    public boolean hasNext() {
        return impl.hasNext();
    }

    @Override
    public T next() {
        return impl.next();
    }

    private static final class Node<N> {
        final N item;
        final Iterator<N> childIterator;

        private Node(N item, Iterator<N> childIterator) {
            this.item = item;
            this.childIterator = childIterator;
        }
    }

    public static final class LoopException extends IllegalArgumentException {
        private final List<?> loop;

        public LoopException(List<?> loop) {
            super(buildMessage(loop));
            this.loop = loop;
        }

        private static String buildMessage(List<?> loop) {
            StringBuilder sb = new StringBuilder();
            for (Object o : loop) {
                sb.append(o).append("->");
            }

            sb.append(loop.get(0));
            return sb.toString();
        }

        public List<?> getLoop() {
            return loop;
        }
    }
}
