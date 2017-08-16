package graphql.sql.core.graph;

import java.util.List;

public final class LoopException extends IllegalArgumentException {
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
