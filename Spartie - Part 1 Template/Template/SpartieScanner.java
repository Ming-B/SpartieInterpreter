package Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpartieScanner {
    private final String source; //string source that determines what is fed into the interpreter

    private int start = 0;
    private int current = 0; //the current token that is being parsed
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fun", TokenType.FUN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("var", TokenType.VAR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("null", TokenType.NULL);
    }

    public SpartieScanner(String source) {
        this.source = source;
    }

    public List<Token> scan() {
        List<Token> tokens = new ArrayList<>();

        Token token = null;
        while (!isAtEnd() && (token = getNextToken()) != null) {
            if (token.type() != TokenType.IGNORE) tokens.add(token);
        }

        return tokens;
    }

    private Token getNextToken() {
        Token token = null;

        // Try to get each type of token, starting with a simple token, and getting a little more complex
        token = getSingleCharacterToken();
        if (token == null) token = getComparisonToken();
        if (token == null) token = getDivideOrComment();
        if (token == null) token = getStringToken();
        if (token == null) token = getNumericToken();
        if (token == null) token = getIdentifierOrReservedWord();
        if (token == null) {
            error(line, String.format("Unexpected character '%c' at %d", source.charAt(current), current));
        }

        return token;
    }

    private Token getSingleCharacterToken() {
        // Hint: Examine the character, if you can get a token, return it, otherwise return null
        // Hint: Be careful with the divide, we have to know if it is a single character

        char nextCharacter = source.charAt(current);

        // Hint: Start of not knowing what the token is, if we can determine it, return it, otherwise, return null
        TokenType type = TokenType.UNDEFINED;

        switch (nextCharacter) {
            case '+':
                type = TokenType.ADD;
                break;
            case '-':
                type = TokenType.SUBTRACT;
                break;
            case '*':
                type = TokenType.MULTIPLY;
                break;
            case ';':
                type = TokenType.SEMICOLON;
                break;
            case ' ':
                type = TokenType.IGNORE;
                break;
            case '\n', '\r', '\t':
                type = TokenType.IGNORE;
                line++;
                break;
            case '(':
                type = TokenType.LEFT_PAREN;
                break;
            case ')':
                type = TokenType.RIGHT_PAREN;
                break;
            case '|':
                type = TokenType.OR;
                break;
            case '{':
                type = TokenType.LEFT_BRACE;
                break;
            case '}':
                type = TokenType.RIGHT_BRACE;
                break;
        }
        if (type != TokenType.UNDEFINED) {
            current++;
            return new Token(type, Character.toString(nextCharacter), line);
        }
        return null;
    }

    // function that determines a comparison token
    private Token getComparisonToken() {
        // Hint: Examine the character for a comparison but check the next character (as long as one is available)
        // For example: < or <=
        start = current;
        char nextCharacter = source.charAt(current);

        TokenType type = TokenType.UNDEFINED;

        if (nextCharacter == '<') {
            if (examine('=')) {
                type = TokenType.LESS_EQUAL;
                //current++;
            } else {
                type = TokenType.LESS_THAN;
            }
        }

        if (nextCharacter == '>') {
            if (examine('=')) {
                type = TokenType.GREATER_EQUAL;
                //current++;
            } else {
                type = TokenType.GREATER_THAN;
            }
        }

        if (nextCharacter == '=') {
            if (examine('=')) {
                type = TokenType.EQUIVALENT;
                //current++;
            } else {
                type = TokenType.ASSIGN;
            }
        }

        if (type != TokenType.UNDEFINED) {
            current++;
            return new Token(type, source.substring(start, current), line);
        }
        return null;
    }


    private Token getDivideOrComment() {
        // Hint: Examine the character for a comparison but check the next character (as long as one is available)
        char nextCharacter = source.charAt(current);
        start = current;
        TokenType type = TokenType.UNDEFINED;

        if (nextCharacter == '/') {
            //either a comment or a division
            if (examine('/')) {
                //entering token = comment
                while (!isAtEnd() && source.charAt(current) != '\n') {
                    current++;
                }
                //line++;
                type = TokenType.IGNORE;
            } else type = TokenType.DIVIDE;
        }

        if (type != TokenType.UNDEFINED) {
            current++;
            return new Token(type, source.substring(start, current), line);
        }
        return null;
    }

    private Token getStringToken() {
        // Hint: Check if you have a double quote, then keep reading until you hit another double quote
        // But, if you do not hit another double quote, you should report an error
        start = current;
        StringBuilder builder = new StringBuilder();


        if (source.charAt(current) != '"') {
            return null;
        }

        current++;

        while (!isAtEnd() && source.charAt(current) != '"') {
            builder.append(source.charAt(current));
            current++;
        }

        if (isAtEnd()) {
            error(line, "No ending double quotes for specified string.");
        }
        current++;

        return new Token(TokenType.STRING, builder.toString(), line);
    }

    private Token getNumericToken() {
        // Hint: Follow similar idea of String, but in this case if it is a digit
        // You should only allow one period in your scanner
        int periodCount = 0;
        StringBuilder builder = new StringBuilder();

        if (!isDigit(source.charAt(current))) {
            return null;
        }

        while (!isAtEnd() && (isDigit(source.charAt(current)) || source.charAt(current) == '.')) {
            builder.append(source.charAt(current));
            current++;
        }

        for (int i = 0; i < builder.toString().length(); i++) {
            if (builder.charAt(i) == '.') {
                periodCount++;
            }
        }
        if (periodCount > 1) {
            error(line, "More than one decimal in number");
        }
        return new Token(TokenType.NUMBER, builder.toString(), line);
    }

    private Token getIdentifierOrReservedWord() {
        // Hint: Assume first it is an identifier and once you capture it, then check if it is a reserved word.
        start = current;
        StringBuilder builder = new StringBuilder();

        if (!isAlpha(source.charAt(current))) {
            return null;
        }

        while (!isAtEnd() && (isAlpha(source.charAt(current)) || isDigit(source.charAt(current)))) {
            builder.append(source.charAt(current));
            current++;
        }

        String text = builder.toString();

        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);

        return new Token(type, text, line);
    }

    // Helper Methods
    private boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private boolean isAlpha(char character) {
        return character >= 'a' && character <= 'z' ||
                character >= 'A' && character <= 'Z';
    }

    // This will check if a character is what you expect, if so, it will advance
    // Useful for checking <= or //
    private boolean examine(char expected) {
        if (isAtEnd()) return false;
        return source.charAt(current + 1) == expected;

        // Otherwise, it matches it, so advance
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Error handling
    private void error(int line, String message) {
        System.err.printf("Error occurred on line %d : %s\n", line, message);
        System.exit(ErrorCode.INTERPRET_ERROR);
    }
}
