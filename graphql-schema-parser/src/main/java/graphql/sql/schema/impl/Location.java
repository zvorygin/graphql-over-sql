package graphql.sql.schema.impl;

import org.antlr.v4.runtime.Token;

class Location {
    private final int line;
    private final int column;

    public Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Location(Token token) {
        this(token.getLine(), token.getCharPositionInLine());
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Location location = (Location) o;

        return line == location.line && column == location.column;
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        return result;
    }

    @Override
    public String toString() {
        return "[" + line + ":" + column + "]";
    }
}
