package graphql.sql.core;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TopologicalIteratorTest {

    @Test
    public void testEmptyCollection() {
        TopologicalIterator<String> iterator = new TopologicalIterator<>(Collections.emptySet(), Collections::<String>singleton);
        List<String> ordered = Lists.newArrayList(iterator);
        Assert.assertEquals(ordered.size(), 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInfiniteLoop() {
        try {
            new TopologicalIterator<>(Collections.singleton("A"), Collections::<String>singleton);
        } catch (TopologicalIterator.LoopException le) {
            List<String> ordered = (List<String>) le.getLoop();
            Assert.assertEquals(Collections.singletonList("A"), ordered);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGoodCase() {
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");

        a.children.add(b);
        a.children.add(c);

        c.children.add(b);

        Iterator<Node> iterator = new TopologicalIterator<>(Arrays.asList(a, b, c), TopologicalIteratorTest::getChildren);
        List<Node> ordered = Lists.newArrayList(iterator);
        Assert.assertEquals(ordered, Arrays.asList(b, c, a));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoopCase() {
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");

        a.children.add(b);
        a.children.add(c);

        b.children.add(a);

        c.children.add(b);

        try {
            new TopologicalIterator<>(Arrays.asList(a, b, c), TopologicalIteratorTest::getChildren);
        } catch (TopologicalIterator.LoopException le) {
            List<Node> ordered = (List<Node>) le.getLoop();
            Assert.assertEquals(Arrays.asList(a, b), ordered);
        }
    }

    private final class Node {
        final String value;

        final List<Node> children = new ArrayList<>();

        public Node(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "{" + value + "}";
        }
    }

    private static List<Node> getChildren(Node n) {
        return n.children;
    }
}