package graphql.sql.core.graph;

import java.util.Iterator;

final class Node<N> {
    final N item;
    final Iterator<N> childIterator;

    Node(N item, Iterator<N> childIterator) {
        this.item = item;
        this.childIterator = childIterator;
    }
}
