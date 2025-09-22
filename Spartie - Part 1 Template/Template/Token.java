package Template;

public record Token(TokenType type, String text, int line, Object literal) {
    public Token(TokenType type, String text, int line) {
        this(type, text, line, null);

    }

    @Override
    public String toString() {
        return String.format("Line: %d Token: %s Text: %s", line, type, text);
    }
}
