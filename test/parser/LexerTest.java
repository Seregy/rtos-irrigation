package parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.*;

public class LexerTest {
    @Test
    void tokenizeOneCommand() throws ParseException {
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize("ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 30, 1, 2, 30-40;");

        Assertions.assertEquals(31, tokens.size());

        List<Token> expected = new ArrayList<>();

        expected.add(new Token(TokenType.STRING, "ПідключитиПолив"));
        expected.add(new Token(TokenType.COLON_SEPARATOR, ":"));
        expected.add(new Token(TokenType.OPEN_BRACKET, "("));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "7"));
        expected.add(new Token(TokenType.HYPHEN_SEPARATOR, "-"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "10"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "12"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "15"));
        expected.add(new Token(TokenType.CLOSE_BRACKET, ")"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "2017"));
        expected.add(new Token(TokenType.HYPHEN_SEPARATOR, "-"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "11"));
        expected.add(new Token(TokenType.HYPHEN_SEPARATOR, "-"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "01"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "10"));
        expected.add(new Token(TokenType.COLON_SEPARATOR, ":"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "10"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "30"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "1"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "2"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "30"));
        expected.add(new Token(TokenType.HYPHEN_SEPARATOR, "-"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "40"));
        expected.add(new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        Assertions.assertEquals(expected, tokens);
    }

    @Test
    void tokenizeMultipleCommands() throws ParseException {
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize("ПоказатиПолив: 1;\nЗадатиПеріодичністьДатчиків: 1, 00:10;\n" +
                "ПоказатиРівеньВологості: (1-5, 7);");

        Assertions.assertEquals(22, tokens.size());

        List<Token> expected = new ArrayList<>();
        expected.add(new Token(TokenType.STRING, "ПоказатиПолив"));
        expected.add(new Token(TokenType.COLON_SEPARATOR, ":"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "1"));
        expected.add(new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        expected.add(new Token(TokenType.STRING, "ЗадатиПеріодичністьДатчиків"));
        expected.add(new Token(TokenType.COLON_SEPARATOR, ":"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "1"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "00"));
        expected.add(new Token(TokenType.COLON_SEPARATOR, ":"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "10"));
        expected.add(new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        expected.add(new Token(TokenType.STRING, "ПоказатиРівеньВологості"));
        expected.add(new Token(TokenType.COLON_SEPARATOR, ":"));
        expected.add(new Token(TokenType.OPEN_BRACKET, "("));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "1"));
        expected.add(new Token(TokenType.HYPHEN_SEPARATOR, "-"));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "5"));
        expected.add(new Token(TokenType.COMMA_SEPARATOR, ","));
        expected.add(new Token(TokenType.INTEGER_NUMBER, "7"));
        expected.add(new Token(TokenType.CLOSE_BRACKET, ")"));
        expected.add(new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        Assertions.assertEquals(expected, tokens);
    }

    @Test
    void throwErrorOnUnknownSymbol() throws Exception {
        Lexer lexer = new Lexer();
        Assertions.assertThrows(ParseException.class, () -> {
            lexer.tokenize("String ?");});
    }
}
