package parser;

import command.*;
import core.Utils;
import core.Utils.SupplierWithParseException;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TokenParser implements Parser {
    private Lexer lexer;
    private ListIterator<Token> iterator;

    public TokenParser(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public List<Command> parse(String input) throws ParseException {
        List<Command> commands = new ArrayList<>();
        List<Token> tokens = lexer.tokenize(input);
        iterator = tokens.listIterator();
        while (iterator.hasNext()) {
            commands.add(parseCommand());
        }
        return commands;
    }

    private Command parseCommand() throws ParseException {
        Token commandToken = expectAndReturn(TokenType.STRING);
        expect(TokenType.COLON_SEPARATOR);

        Command command;
        int[] zones;
        LocalDateTime dateTime;
        LocalTime wateringInterval;
        Integer waterVolume;
        Integer wateringDuration;
        LocalTime sensorInterval;
        Integer fertilizerVolume;

        switch (commandToken.getValue()) {
            case EnableWatering.NAME:
                zones = consumeIntOrIntGroup();
                expect(TokenType.COMMA_SEPARATOR);
                dateTime = consumeDateTime();
                expect(TokenType.COMMA_SEPARATOR);
                wateringInterval = consumeTime();
                expect(TokenType.COMMA_SEPARATOR);
                waterVolume = consumeInt();
                expect(TokenType.COMMA_SEPARATOR);
                wateringDuration = consumeInt();
                expect(TokenType.COMMA_SEPARATOR);
                Map.Entry<Integer, Integer> humidity = consumeIntRange();
                command = new EnableWatering(zones, dateTime, wateringInterval, waterVolume, wateringDuration, humidity);
                break;
            case ShowWatering.NAME:
                zones = consumeIntOrIntGroup();
                command = new ShowWatering(zones);
                break;
            case StopWatering.NAME:
                zones = consumeIntOrIntGroup();
                command = new StopWatering(zones);
                break;
            case ResumeWatering.NAME:
                zones = consumeIntOrIntGroup();
                command = new ResumeWatering(zones);
                break;
            case ChangeWatering.NAME:
                zones = consumeIntOrIntGroup();
                expect(TokenType.COMMA_SEPARATOR);
                dateTime = consumeOptional(this::consumeDateTime, TokenType.INTEGER_NUMBER).orElse(null);
                expect(TokenType.COMMA_SEPARATOR);
                wateringInterval = consumeOptional(this::consumeTime, TokenType.INTEGER_NUMBER).orElse(null);
                expect(TokenType.COMMA_SEPARATOR);
                waterVolume = consumeOptional(this::consumeInt, TokenType.INTEGER_NUMBER).orElse(null);
                expect(TokenType.COMMA_SEPARATOR);
                wateringDuration = consumeOptional(this::consumeInt, TokenType.INTEGER_NUMBER).orElse(null);
                expect(TokenType.COMMA_SEPARATOR);
                humidity = consumeOptional(this::consumeIntRange, TokenType.INTEGER_NUMBER).orElse(null);
                command = new ChangeWatering(zones, dateTime, wateringInterval, waterVolume, wateringDuration, humidity);
                break;
            case SetSensorPeriodicity.NAME:
                zones = consumeIntOrIntGroup();
                expect(TokenType.COMMA_SEPARATOR);
                sensorInterval = consumeTime();
                command = new SetSensorPeriodicity(zones, sensorInterval);
                break;
            case ShowHumidity.NAME:
                zones = consumeIntOrIntGroup();
                command = new ShowHumidity(zones);
                break;
            case EnableFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                expect(TokenType.COMMA_SEPARATOR);
                fertilizerVolume = consumeInt();
                command = new EnableFertilizing(zones, fertilizerVolume);
                break;
            case ShowFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                command = new ShowFertilizing(zones);
                break;
            case ChangeFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                expect(TokenType.COMMA_SEPARATOR);
                fertilizerVolume = consumeInt();
                command = new ChangeFertilizing(zones, fertilizerVolume);
                break;
            case StopFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                command = new StopFertilizing(zones);
                break;
            default:
                throw new ParseException("Unknown command: " + commandToken.getValue(), iterator.previousIndex() + 1);

        }
        expect(TokenType.SEMICOLON_SEPARATOR);

        return command;
    }

    private int consumeInt() throws ParseException {
        Token token = expectAndReturn(TokenType.INTEGER_NUMBER);
        return Integer.parseInt(token.getValue());
    }

    private int[] consumeIntGroup() throws ParseException {
        List<Integer> integers = new ArrayList<>();

        expect(TokenType.OPEN_BRACKET);
        TokenType nextTokenType = peekType();
        while (nextTokenType != TokenType.CLOSE_BRACKET) {
            if (nextTokenType == TokenType.INTEGER_NUMBER) {
                integers.add(consumeInt());
            } else if (nextTokenType == TokenType.HYPHEN_SEPARATOR) {
                integers.remove(integers.size() - 1);
                iterator.previous();
                Map.Entry<Integer, Integer> range = consumeIntRange();
                IntStream
                        .range(range.getKey(), range.getValue() + 1)
                        .boxed()
                        .collect(Collectors.toCollection(() -> integers));
            } else {
                expect(TokenType.COMMA_SEPARATOR);
            }
            nextTokenType = peekType();
        }
        expect(TokenType.CLOSE_BRACKET);

        return integers.stream().mapToInt(i->i).toArray();
    }

    private int[] consumeIntOrIntGroup() throws ParseException {
        if (peekType() == TokenType.INTEGER_NUMBER) {
            return new int[] {consumeInt()};
        }

        return consumeIntGroup();
    }

    private Map.Entry<Integer, Integer> consumeIntRange() throws ParseException {
        Token firstNumeric = expectAndReturn(TokenType.INTEGER_NUMBER);
        int first = Integer.parseInt(firstNumeric.getValue());

        expect(TokenType.HYPHEN_SEPARATOR);

        Token secondNumeric = expectAndReturn(TokenType.INTEGER_NUMBER);
        int second = Integer.parseInt(secondNumeric.getValue());

        return new AbstractMap.SimpleImmutableEntry<>(first, second);
    }

    private LocalDateTime consumeDateTime() throws ParseException {
        Token year = expectAndReturn(TokenType.INTEGER_NUMBER);
        expect(TokenType.HYPHEN_SEPARATOR);
        Token month = expectAndReturn(TokenType.INTEGER_NUMBER);
        expect(TokenType.HYPHEN_SEPARATOR);
        Token day = expectAndReturn(TokenType.INTEGER_NUMBER);
        Token hour = expectAndReturn(TokenType.INTEGER_NUMBER);
        expect(TokenType.COLON_SEPARATOR);
        Token minute = expectAndReturn(TokenType.INTEGER_NUMBER);

        return LocalDateTime.of(Integer.parseInt(year.getValue()),
                Integer.parseInt(month.getValue()),
                Integer.parseInt(day.getValue()),
                Integer.parseInt(hour.getValue()),
                Integer.parseInt(minute.getValue()));
    }

    private LocalTime consumeTime() throws ParseException {
        Token hour = expectAndReturn(TokenType.INTEGER_NUMBER);
        expect(TokenType.COLON_SEPARATOR);
        Token minute = expectAndReturn(TokenType.INTEGER_NUMBER);

        return LocalTime.of(Integer.parseInt(hour.getValue()),
                Integer.parseInt(minute.getValue()));
    }

    private void expect(TokenType type) throws ParseException {
        expectAndReturn(type);
    }

    private Token expectAndReturn(TokenType type) throws ParseException {
        Token token;
        try {
            token = iterator.next();
        } catch (NoSuchElementException nsee) {
            throw new ParseException("Couldn't find token type: " + type, iterator.previousIndex());
        }
        if (token.getType() != type) {
            throw new ParseException("Wrong token type: " + token.getType() + ", expected " + type, iterator.previousIndex() + 1);
        }
        return token;
    }

    private <T> Optional<T> consumeOptional(SupplierWithParseException<T> supplier, TokenType expectedType)
            throws ParseException {
        Optional<T> value = Optional.empty();
        if (peekType() == expectedType) {
            value = Optional.of(supplier.get());
        }

        return value;
    }

    private TokenType peekType() {
        return Utils.peek(iterator).getType();
    }
}
