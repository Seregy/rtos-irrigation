package parser;

import command.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.*;

public class TokenParserTest {
    @Test
    void parseEnableWateringCommand() throws ParseException {
        List<Token> tokens = Arrays.asList(new Token(TokenType.STRING, "ПідключитиПолив"),
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
                new Token(TokenType.INTEGER_NUMBER, "00"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "30"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "2"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "30"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "40"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        String input = "ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;";

        Lexer mockedLexer = mock(Lexer.class);
        when(mockedLexer.tokenize(input)).thenReturn(tokens);

        TokenParser parser = new TokenParser(mockedLexer);
        Command command = parser.parse(input).get(0);

        Assertions.assertTrue(command instanceof EnableWatering);
        Assertions.assertEquals(command.getName(), "ПідключитиПолив");

        EnableWatering enableWatering = (EnableWatering) command;

        Assertions.assertArrayEquals(new int[]{7, 8, 9, 10, 12, 15}, enableWatering.getZones());
        Assertions.assertEquals(LocalDateTime.of(2017, 11, 1, 10, 10),
                enableWatering.getFirstWatering());
        Assertions.assertEquals(LocalTime.of(0,30), enableWatering.getWateringInterval());
        Assertions.assertEquals(1, enableWatering.getWaterVolume());
        Assertions.assertEquals(2, enableWatering.getWateringDuration());
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<>(30, 40);
        Assertions.assertEquals(range, enableWatering.getHumidityRange());
    }

    @Test
    void parseChangeWateringCommandWithOmittedParams() throws ParseException {
        List<Token> tokens = Arrays.asList(new Token(TokenType.STRING, "ЗмінитиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "00"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "05"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "5"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "10"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        String input = "ЗмінитиПолив: 1, , 00:05, , , 5-10;";

        Lexer mockedLexer = mock(Lexer.class);
        when(mockedLexer.tokenize(input)).thenReturn(tokens);
        TokenParser parser = new TokenParser(mockedLexer);

        Command command = parser.parse(input).get(0);

        Assertions.assertTrue(command instanceof ChangeWatering);
        Assertions.assertEquals(command.getName(), "ЗмінитиПолив");

        ChangeWatering changeWatering = (ChangeWatering) command;

        Assertions.assertArrayEquals(new int[]{1}, changeWatering.getZones());
        Assertions.assertNull(changeWatering.getFirstWatering());
        Assertions.assertEquals(LocalTime.of(0,5), changeWatering.getWateringInterval());
        Assertions.assertNull(changeWatering.getWaterVolume());
        Assertions.assertNull(changeWatering.getWateringDuration());
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<>(5, 10);
        Assertions.assertEquals(range, changeWatering.getHumidityRange());
    }

    @Test
    void parseMultipleCommands() throws ParseException {
        List<Token> tokens = Arrays.asList(new Token(TokenType.STRING, "ПідключитиПолив"),
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
                new Token(TokenType.INTEGER_NUMBER, "00"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "30"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "2"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "30"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "40"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"),
                new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.OPEN_BRACKET, "("),
                new Token(TokenType.INTEGER_NUMBER, "7"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "9"),
                new Token(TokenType.CLOSE_BRACKET, ")"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"),
                new Token(TokenType.STRING, "ЗупинитиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "5"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        String input = "ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ПоказатиПолив: (7, 9);\nЗупинитиПолив: 5;";

        Lexer mockedLexer = mock(Lexer.class);
        when(mockedLexer.tokenize(input)).thenReturn(tokens);


        TokenParser parser = new TokenParser(mockedLexer);
        List<Command> list = parser.parse(input);

        Command first = list.get(0);
        Assertions.assertTrue(first instanceof EnableWatering);
        EnableWatering enableWatering = (EnableWatering) first;
        Assertions.assertArrayEquals(new int[]{7, 8, 9, 10, 12, 15}, enableWatering.getZones());

        Command second = list.get(1);
        Assertions.assertTrue(second instanceof ShowWatering);
        ShowWatering showWatering = (ShowWatering) second;
        Assertions.assertArrayEquals(new int[]{7, 9}, showWatering.getZones());

        Command third = list.get(2);
        Assertions.assertTrue(third instanceof StopWatering);
        StopWatering stopWatering = (StopWatering) third;
        Assertions.assertArrayEquals(new int[]{5}, stopWatering.getZones());
    }

    @Test
    void throwErrorOnUnknownCommand() throws ParseException {
        List<Token> tokens = Arrays.asList( new Token(TokenType.STRING, "Command"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        String input = "Command: 1;";

        Lexer mockedLexer = mock(Lexer.class);
        when(mockedLexer.tokenize(input)).thenReturn(tokens);

        TokenParser parser = new TokenParser(mockedLexer);
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input));
    }

    @Test
    void throwErrorOnWrongParamsAmount() throws ParseException {
        List<Token> tokens1 = Arrays.asList( new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "2"),
                new Token(TokenType.COMMA_SEPARATOR, ","),
                new Token(TokenType.INTEGER_NUMBER, "3"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));
        List<Token> tokens2 = Arrays.asList( new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        String input1 = "ПоказатиПолив: 1, 2, 3;";
        String input2 = "ПоказатиПолив: ;";

        Lexer mockedLexer = mock(Lexer.class);
        when(mockedLexer.tokenize(input1)).thenReturn(tokens1);
        when(mockedLexer.tokenize(input2)).thenReturn(tokens2);

        TokenParser parser = new TokenParser(mockedLexer);
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input1));
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input2));
    }

    @Test
    void throwErrorOnWrongParamsType() throws ParseException {
        List<Token> tokens1 = Arrays.asList( new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.HYPHEN_SEPARATOR, "-"),
                new Token(TokenType.INTEGER_NUMBER, "2"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));
        List<Token> tokens2 = Arrays.asList( new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "10"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "20"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));
        List<Token> tokens3 = Arrays.asList( new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.STRING, "string"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));

        String input1 = "ПоказатиПолив: 1-2;";
        String input2 = "ПоказатиПолив: 10:20;";
        String input3 = "ПоказатиПолив: string;";

        Lexer mockedLexer = mock(Lexer.class);
        when(mockedLexer.tokenize(input1)).thenReturn(tokens1);
        when(mockedLexer.tokenize(input2)).thenReturn(tokens2);
        when(mockedLexer.tokenize(input3)).thenReturn(tokens3);

        TokenParser parser = new TokenParser(mockedLexer);
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input1));
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input2));
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input3));
    }

    @Test
    void throwErrorOnIncorrectSyntax() throws ParseException {
        List<Token> tokens1 = Arrays.asList( new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.INTEGER_NUMBER, "1"),
                new Token(TokenType.SEMICOLON_SEPARATOR, ";"));
        List<Token> tokens2 = Arrays.asList( new Token(TokenType.STRING, "ПоказатиПолив"),
                new Token(TokenType.COLON_SEPARATOR, ":"),
                new Token(TokenType.INTEGER_NUMBER, "1"));

        String input1 = "ПоказатиПолив 1;";
        String input2 = "ПоказатиПолив: 1";

        Lexer mockedLexer = mock(Lexer.class);
        when(mockedLexer.tokenize(input1)).thenReturn(tokens1);
        when(mockedLexer.tokenize(input2)).thenReturn(tokens2);

        TokenParser parser = new TokenParser(mockedLexer);
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input1));
        Assertions.assertThrows(ParseException.class, () -> parser.parse(input2));
    }
}
