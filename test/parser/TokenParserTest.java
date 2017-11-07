package parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import command.Command;
import command.EnableWatering;
import command.ShowWatering;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TokenParserTest {
    @Test
    void parseEnableWateringCommand() {
        TokenParser parser = new TokenParser(new Lexer());
        List<Command> list = parser.parse("ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;");
        Command command = list.get(0);

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
    void parseMultipleCommands() {
        TokenParser parser = new TokenParser(new Lexer());
        List<Command> list = parser.parse("ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ПоказатиПолив: (7, 9);");

        Command first = list.get(0);
        Assertions.assertTrue(first instanceof EnableWatering);
        EnableWatering enableWatering = (EnableWatering) first;
        Assertions.assertArrayEquals(new int[]{7, 8, 9, 10, 12, 15}, enableWatering.getZones());

        Command second = list.get(1);
        Assertions.assertTrue(second instanceof ShowWatering);
        ShowWatering showWatering = (ShowWatering) second;
        Assertions.assertArrayEquals(new int[]{7, 9}, showWatering.getZones());
    }
}
