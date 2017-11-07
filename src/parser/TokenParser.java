package parser;

import command.*;
import core.Utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TokenParser implements Parser {
    private Lexer lexer;
    private ListIterator<Token> iterator;

    public TokenParser(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public List<Command> parse(String input) {
        List<Command> commands = new ArrayList<>();
        List<Token> tokens = lexer.tokenize(input);
        iterator = tokens.listIterator();
        while (iterator.hasNext()) {
            commands.add(parseCommand());
        }
        return commands;
    }

    private Command parseCommand() {
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
                sensorInterval = consumeTime();
                command = new SetSensorPeriodicity(zones, sensorInterval);
                break;
            case ShowHumidity.NAME:
                zones = consumeIntOrIntGroup();
                command = new ShowHumidity(zones);
                break;
            case EnableFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                fertilizerVolume = consumeInt();
                command = new EnableFertilizing(zones, fertilizerVolume);
                break;
            case ShowFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                command = new ShowFertilizing(zones);
                break;
            case ChangeFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                fertilizerVolume = consumeInt();
                command = new ChangeFertilizing(zones, fertilizerVolume);
                break;
            case StopFertilizing.NAME:
                zones = consumeIntOrIntGroup();
                command = new StopFertilizing(zones);
                break;
            default:
                throw new RuntimeException("Unknown command: " + commandToken.getValue());

        }
        expect(TokenType.SEMICOLON_SEPARATOR);

        return command;
    }

    private int consumeInt() {
        Token token = expectAndReturn(TokenType.INTEGER_NUMBER);
        return Integer.parseInt(token.getValue());
    }

    private int[] consumeIntGroup() {
        List<Integer> integers = new ArrayList<>();

        expect(TokenType.OPEN_BRACKET);
        Token current = Utils.peek(iterator);
        while (current.getType() != TokenType.CLOSE_BRACKET) {
            if (current.getType() == TokenType.INTEGER_NUMBER) {
                integers.add(consumeInt());
            } else if (current.getType() == TokenType.HYPHEN_SEPARATOR) {
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
            current = Utils.peek(iterator);
        }
        expect(TokenType.CLOSE_BRACKET);

        return integers.stream().mapToInt(i->i).toArray();
    }

    private int[] consumeIntOrIntGroup() {
        if (Utils.peek(iterator).getType() == TokenType.INTEGER_NUMBER) {
            return new int[] {consumeInt()};
        }

        return consumeIntGroup();
    }

    private Map.Entry<Integer, Integer> consumeIntRange() {
        Token firstNumeric = expectAndReturn(TokenType.INTEGER_NUMBER);
        int first = Integer.parseInt(firstNumeric.getValue());

        expect(TokenType.HYPHEN_SEPARATOR);

        Token secondNumeric = expectAndReturn(TokenType.INTEGER_NUMBER);
        int second = Integer.parseInt(secondNumeric.getValue());

        return new AbstractMap.SimpleImmutableEntry<>(first, second);
    }

    private LocalDateTime consumeDateTime() {
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

    private LocalTime consumeTime() {
        Token hour = expectAndReturn(TokenType.INTEGER_NUMBER);
        expect(TokenType.COLON_SEPARATOR);
        Token minute = expectAndReturn(TokenType.INTEGER_NUMBER);

        return LocalTime.of(Integer.parseInt(hour.getValue()),
                Integer.parseInt(minute.getValue()));
    }

    private void expect(TokenType type) {
        expectAndReturn(type);
    }

    private Token expectAndReturn(TokenType type) {
        Token token = iterator.next();
        if (token.getType() != type) {
            throw new RuntimeException("Wrong token type: " + token.getType());
        }
        return token;
    }

    private <T> Optional<T> consumeOptional(Supplier<T> supplier, TokenType expectedType) {
        Optional<T> value = Optional.empty();
        if (Utils.peek(iterator).getType() == expectedType) {
            value = Optional.of(supplier.get());
        }

        return value;
    }
}
