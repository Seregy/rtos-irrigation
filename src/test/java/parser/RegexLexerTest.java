package parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.*;

public class RegexLexerTest {
    @Test
    void tokenizeOneCommand() throws ParseException {
        RegexLexer lexer = new RegexLexer();
        List<Token> tokens = lexer.tokenize("ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 30, 1, 1.5, 30-40;");

        Assertions.assertEquals(33, tokens.size());

        List<Token> expected = Arrays.asList(new Token(TokenType.STRING, "ПідключитиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.OPEN_BRACKET, "("),
                new Token(TokenType.INTEGER_NUMBER, "7"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "10"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "12"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "15"),
                new Token(TokenType.CLOSE_BRACKET, ")"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "2017"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "11"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "01"),
                new Token(TokenType.INTEGER_NUMBER, "10"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "10"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "30"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.DOT_SEPARATOR, "."),
                new Token(TokenType.INTEGER_NUMBER, "5"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "30"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "40"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        Assertions.assertEquals(expected, tokens);
    }

    @Test
    void tokenizeMultipleCommands() throws ParseException {
        RegexLexer lexer = new RegexLexer();
        List<Token> tokens = lexer.tokenize("ПоказатиПолив: 1;\nЗадатиПеріодичністьДатчиків: 1, 00:10;\n" +
                "ПоказатиРівеньВологості: (1-5, 7);");

        Assertions.assertEquals(22, tokens.size());

        List<Token> expected = Arrays.asList(new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"),
                new Token(TokenType.STRING, "ЗадатиПеріодичністьДатчиків"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "00"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "10"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"),
                new Token(TokenType.STRING, "ПоказатиРівеньВологості"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.OPEN_BRACKET, "("),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "5"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "7"),
                new Token(TokenType.CLOSE_BRACKET, ")"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        Assertions.assertEquals(expected, tokens);
    }

    @Test
    void throwErrorOnUnknownSymbol() throws Exception {
        RegexLexer lexer = new RegexLexer();
        Assertions.assertThrows(ParseException.class, () -> {
            lexer.tokenize("String ?");});
    }
}
