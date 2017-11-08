package parser;

import java.text.ParseException;
import java.util.List;

public interface Lexer {
    List<Token> tokenize(String input) throws ParseException;
}
