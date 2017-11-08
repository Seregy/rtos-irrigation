package parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class RegexLexer implements Lexer {
    public List<Token> tokenize(String string) throws ParseException {
        List<Token> tokens = new ArrayList<>();
        String str = string;
        while (!str.isEmpty()) {
            boolean matched = false;
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

            if (!matched) {
                throw new ParseException("Unexpected character: " + str, string.indexOf(str));
            }
        }

        return tokens;
    }
}
