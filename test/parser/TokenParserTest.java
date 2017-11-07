package parser;

import command.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TokenParserTest {
    @Test
    void parseEnableWateringCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ПідключитиПолив: (7-10, 12, 15)," +
                " 2017-11-01 10:10, 00:30, 1, 2, 30-40;").get(0);

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
    void parseShowWateringCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ПоказатиПолив: 1;").get(0);

        Assertions.assertTrue(command instanceof ShowWatering);
        Assertions.assertEquals(command.getName(), "ПоказатиПолив");

        ShowWatering showWatering = (ShowWatering) command;

        Assertions.assertArrayEquals(new int[]{1}, showWatering.getZones());
    }

    @Test
    void parseStopWateringCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ЗупинитиПолив: 1;").get(0);

        Assertions.assertTrue(command instanceof StopWatering);
        Assertions.assertEquals(command.getName(), "ЗупинитиПолив");

        StopWatering stopWatering = (StopWatering) command;

        Assertions.assertArrayEquals(new int[]{1}, stopWatering.getZones());
    }

    @Test
    void parseResumeWateringCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ВідновитиПолив: 1;").get(0);

        Assertions.assertTrue(command instanceof ResumeWatering);
        Assertions.assertEquals(command.getName(), "ВідновитиПолив");

        ResumeWatering resumeWatering = (ResumeWatering) command;

        Assertions.assertArrayEquals(new int[]{1}, resumeWatering.getZones());
    }

    @Test
    void parseChangeWateringCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ЗмінитиПолив: 1," +
                " 2017-01-01 01:00, 00:01, 1, 2, 10-20;").get(0);

        Assertions.assertTrue(command instanceof ChangeWatering);
        Assertions.assertEquals(command.getName(), "ЗмінитиПолив");

        ChangeWatering changeWatering = (ChangeWatering) command;

        Assertions.assertArrayEquals(new int[]{1}, changeWatering.getZones());
        Assertions.assertEquals(LocalDateTime.of(2017, 1, 1, 1, 0),
                changeWatering.getFirstWatering());
        Assertions.assertEquals(LocalTime.of(0,1), changeWatering.getWateringInterval());
        Assertions.assertEquals(Integer.valueOf(1), changeWatering.getWaterVolume());
        Assertions.assertEquals(Integer.valueOf(2), changeWatering.getWateringDuration());
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<>(10, 20);
        Assertions.assertEquals(range, changeWatering.getHumidityRange());
    }

    @Test
    void parseChangeWateringCommandWithOmittedParams() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ЗмінитиПолив: 1," +
                " , 00:05, , , 5-10;").get(0);

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
    void parseSetSensorPeriodicityCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ЗадатиПеріодичністьДатчиків: 1, 00:10;").get(0);

        Assertions.assertTrue(command instanceof SetSensorPeriodicity);
        Assertions.assertEquals(command.getName(), "ЗадатиПеріодичністьДатчиків");

        SetSensorPeriodicity setSensorPeriodicity = (SetSensorPeriodicity) command;

        Assertions.assertArrayEquals(new int[]{1}, setSensorPeriodicity.getZones());
        Assertions.assertEquals(LocalTime.of(0, 10), setSensorPeriodicity.getCheckInterval());
    }

    @Test
    void parseShowHumidityCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ПоказатиРівеньВологості: 1;").get(0);

        Assertions.assertTrue(command instanceof ShowHumidity);
        Assertions.assertEquals(command.getName(), "ПоказатиРівеньВологості");

        ShowHumidity showHumidity = (ShowHumidity) command;

        Assertions.assertArrayEquals(new int[]{1}, showHumidity.getZones());
    }

    @Test
    void parseEnableFertilizingCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ПідключитиУдобрювання: 1, 5;").get(0);

        Assertions.assertTrue(command instanceof EnableFertilizing);
        Assertions.assertEquals(command.getName(), "ПідключитиУдобрювання");

        EnableFertilizing enableFertilizing = (EnableFertilizing) command;

        Assertions.assertArrayEquals(new int[]{1}, enableFertilizing.getZones());
        Assertions.assertEquals(5, enableFertilizing.getFertilizerVolume());
    }

    @Test
    void parseShowFertilizingCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ПоказатиУдобрювання: 1;").get(0);

        Assertions.assertTrue(command instanceof ShowFertilizing);
        Assertions.assertEquals(command.getName(), "ПоказатиУдобрювання");

        ShowFertilizing showFertilizing = (ShowFertilizing) command;

        Assertions.assertArrayEquals(new int[]{1}, showFertilizing.getZones());
    }

    @Test
    void parseChangeFertilizingCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ЗмінитиУдобрювання: 1, 7;").get(0);

        Assertions.assertTrue(command instanceof ChangeFertilizing);
        Assertions.assertEquals(command.getName(), "ЗмінитиУдобрювання");

        ChangeFertilizing changeFertilizing = (ChangeFertilizing) command;

        Assertions.assertArrayEquals(new int[]{1}, changeFertilizing.getZones());
        Assertions.assertEquals(7, changeFertilizing.getFertilizerVolume());
    }

    @Test
    void parseStopFertilizingCommand() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        Command command = parser.parse("ЗупинитиУдобрювання: 1;").get(0);

        Assertions.assertTrue(command instanceof StopFertilizing);
        Assertions.assertEquals(command.getName(), "ЗупинитиУдобрювання");

        StopFertilizing stopFertilizing = (StopFertilizing) command;

        Assertions.assertArrayEquals(new int[]{1}, stopFertilizing.getZones());
    }

    @Test
    void parseMultipleCommands() throws ParseException {
        TokenParser parser = new TokenParser(new Lexer());
        List<Command> list = parser.parse("ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ПоказатиПолив: (7, 9);\nЗупинитиПолив: 5;");

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
    void throwErrorOnUnknownCommand() {
        TokenParser parser = new TokenParser(new Lexer());
        Assertions.assertThrows(ParseException.class, () -> parser.parse("Command: 1;"));
    }

    @Test
    void throwErrorOnWrongParamsAmount() {
        TokenParser parser = new TokenParser(new Lexer());
        Assertions.assertThrows(ParseException.class, () -> parser.parse("ПоказатиПолив: 1, 2, 3;"));
        Assertions.assertThrows(ParseException.class, () -> parser.parse("ПоказатиПолив: ;"));
    }

    @Test
    void throwErrorOnWrongParamsType() {
        TokenParser parser = new TokenParser(new Lexer());
        Assertions.assertThrows(ParseException.class, () -> parser.parse("ПоказатиПолив: 1-2;"));
        Assertions.assertThrows(ParseException.class, () -> parser.parse("ПоказатиПолив: 10:20;"));
        Assertions.assertThrows(ParseException.class, () -> parser.parse("ПоказатиПолив: string;"));
    }

    @Test
    void throwErrorOnIncorrectSyntax() {
        TokenParser parser = new TokenParser(new Lexer());
        Assertions.assertThrows(ParseException.class, () -> parser.parse("ПоказатиПолив 1;"));
        Assertions.assertThrows(ParseException.class, () -> parser.parse("ПоказатиПолив: 1"));
    }
}
