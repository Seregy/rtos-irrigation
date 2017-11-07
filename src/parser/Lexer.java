package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Lexer {
    public List<Token> tokenize(String string) {
        List<Token> tokens = new ArrayList<>();
        String str = string;
        boolean matched = false;
        while (!str.isEmpty()) {
            for (TokenType type : TokenType.values()) {
                Matcher matcher = type.getPattern().matcher(str);
                if (matcher.find()) {
                    matched = true;
                    String value = matcher.group();
                    str = matcher.replaceFirst("");

                    if (type != TokenType.WHITESPACE_SEPARATOR) {
                        tokens.add(new Token(type, value));
                    }

                    break;
                }
            }
        }

        if (!matched) {
            throw new RuntimeException("Unexpected character: " + string);
        }

        return tokens;
    }
}
