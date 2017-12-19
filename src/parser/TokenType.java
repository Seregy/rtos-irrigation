package parser;

import java.util.regex.Pattern;

public enum TokenType {
    STRING("\\p{alpha}+"),
    COLON_SEPARATOR(":"),
    SEMICOLON_SEPARATOR(";"),
    COMMA_SEPARATOR(","),
    DOT_SEPARATOR("\\."),
    HYPHEN_SEPARATOR("-"),
    WHITESPACE_SEPARATOR("\\s+"),
    OPEN_BRACKET("\\("),
    CLOSE_BRACKET("\\)"),
    INTEGER_NUMBER("\\d+");

    private Pattern pattern;

    TokenType(String regex) {
        this.pattern = Pattern.compile("^(" + regex + ")", Pattern.UNICODE_CHARACTER_CLASS);
    }

    Pattern getPattern() {
        return pattern;
    }
}
