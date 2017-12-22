package core;

import command.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import ui.MainWindow;
import zone.FertilizingStatus;
import zone.WateringStatus;
import zone.Zone;
import zone.ZoneDAO;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.*;

public class AppHandleCommandsTest {
    @Test
    void handleEnableWatering() throws ParseException{
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;";
        List<Command> commands = Collections.singletonList(new EnableWatering(new int[]{7, 8, 9, 10, 12, 15},
                LocalDateTime.of(2017, 11, 1, 10, 10),
                LocalTime.of(0, 30), 1, 2,
                new AbstractMap.SimpleImmutableEntry<>(30, 40)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(WateringStatus.NOT_INITIALISED, mockedDAO.find(6).getWateringStatus());
        Assertions.assertEquals(WateringStatus.ENABLED, mockedDAO.find(7).getWateringStatus());
        Assertions.assertEquals(WateringStatus.ENABLED, mockedDAO.find(8).getWateringStatus());
        Assertions.assertEquals(WateringStatus.ENABLED, mockedDAO.find(12).getWateringStatus());
    }

    @Test
    void handleShowWatering() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 7, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ПоказатиПолив: (5, 7);";
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{7},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ShowWatering(new int[] {5, 7}));
        when(mockedParser.parse(input)).thenReturn(commands);

        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);

        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Zone 5: watering enabled - false")));
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Zone 7: watering enabled - true")));
    }

    @Test
    void handleStopWatering() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 7, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "\nЗупинитиПолив: (3, 7);";
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{7},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new StopWatering(new int[] {3, 7}));
        when(mockedParser.parse(input)).thenReturn(commands);

        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);

        Assertions.assertEquals(WateringStatus.DISABLED, mockedDAO.find(7).getWateringStatus());
        Assertions.assertEquals(WateringStatus.NOT_INITIALISED, mockedDAO.find(3).getWateringStatus());
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Stop watering zone 7")));
        verify(mockedWindow, times(0)).print(argThat(s -> s.startsWith("Stop watering zone 3")));
    }

    @Test
    void handleResumeWatering() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: (7, 8), 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗупинитиПолив: 7;" + "ВідновитиПолив: (3, 7, 8);";
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{7, 8},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new StopWatering(new int[] {7}),
                new ResumeWatering(new int[] {3, 7, 8}));
        when(mockedParser.parse(input)).thenReturn(commands);

        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);

        Assertions.assertEquals(WateringStatus.ENABLED, mockedDAO.find(7).getWateringStatus());
        Assertions.assertEquals(WateringStatus.NOT_INITIALISED, mockedDAO.find(3).getWateringStatus());
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Resuming watering zone 7")));
        verify(mockedWindow, times(0)).print(argThat(s -> s.startsWith("Resuming watering zone 3")));
        verify(mockedWindow, times(0)).print(argThat(s -> s.startsWith("Resuming watering zone 8")));
    }

    @Test
    void handleChangeWatering() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, 2, 3, 10-20;";

        LocalTime interval = LocalTime.of(0, 15);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                LocalDateTime.of(2017, 11, 1, 10, 10),
                LocalTime.of(0, 30), 1, 2,
                new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        interval, 2, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(interval, mockedDAO.find(10).getWateringInterval());
    }

    @Test
    void handleChangeWateringWithoutFirstWatering() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , 00:15, 2, 3, 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date,
                        LocalTime.of(0, 30), 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        null,
                        LocalTime.of(0, 15), 2, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(LocalTime.of(0, 15), mockedDAO.find(10).getWateringInterval());
    }

    @Test
    void handleChangeWateringWithoutWateringInterval() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, , 2, 3, 10-20;";


        LocalTime interval = LocalTime.of(0, 30);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        interval, 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        null, 2, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(interval, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(2, mockedDAO.find(10).getWaterVolume());
    }

    @Test
    void handleChangeWateringWithoutWaterVolume() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, , 3, 10-20;";

        int volume = 1;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), volume, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        LocalTime.of(0, 15), null, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(volume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutWateringDuration() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, 2, , 10-20;";

        double duration = 2;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), 1, duration,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        LocalTime.of(0, 15), 2, null,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(duration, mockedDAO.find(10).getWateringDuration());
        Assertions.assertEquals(2, mockedDAO.find(10).getWaterVolume());
    }

    @Test
    void handleChangeWateringWithoutHumidityRange() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, 2, 3, ;";

        Map.Entry<Integer, Integer> humidityRange = new AbstractMap.SimpleImmutableEntry<>(30, 40);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), 1, 2,
                        humidityRange),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        LocalTime.of(0, 15), 2, 3.0, null));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(humidityRange, mockedDAO.find(10).getHumidityRange());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutFirstWateringAndWateringInterval() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:01, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , , 2, 3, 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        LocalTime time = LocalTime.of(0, 1);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date, time, 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        null,
                        null, 2, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(time, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(2, mockedDAO.find(10).getWaterVolume());
    }

    @Test
    void handleChangeWateringWithoutFirstWateringAndWaterVolume() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:01, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , 00:15, , 3, 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        int volume = 1;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date, LocalTime.of(0, 1), volume, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        null,
                        LocalTime.of(0, 15), null, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(volume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutFirstWateringAndWateringDuration() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:01, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , 00:15, 2, , 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        double duration = 2.0;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date, LocalTime.of(0, 1), 1, duration,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        null,
                        LocalTime.of(0, 15), 2, null,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(duration, mockedDAO.find(10).getWateringDuration());
        Assertions.assertEquals(2, mockedDAO.find(10).getWaterVolume());
    }

    @Test
    void handleChangeWateringWithoutFirstWateringAndHumidityRange() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:01, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , 00:15, 2, , 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<Integer, Integer>(30, 40);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date, LocalTime.of(0, 1), 1, 2.0,
                        range),
                new ChangeWatering(new int[] {10},
                        null,
                        LocalTime.of(0, 15), 2, 3.0,
                        null));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(range, mockedDAO.find(10).getHumidityRange());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutWateringIntervalAndWaterVolume() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, , , 3, 10-20;";

        LocalTime interval = LocalTime.of(0, 30);
        int volume = 1;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        interval, volume, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        null, null, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(interval, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(volume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutWateringIntervalAndWateringDuration() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, , 2, , 10-20;";

        LocalTime interval = LocalTime.of(0, 30);
        double duration = 1;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        interval, 1, duration,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        null, 2, null,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(interval, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(duration, mockedDAO.find(10).getWateringDuration());
        Assertions.assertEquals(2, mockedDAO.find(10).getWaterVolume());
    }

    @Test
    void handleChangeWateringWithoutWateringIntervalAndHumidityRange() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, , 2, 3, ;";

        LocalTime interval = LocalTime.of(0, 30);
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<Integer, Integer>(30, 40);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        interval, 1, 2,
                        range),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        null, 2, 3.0,
                        null));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(interval, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(range, mockedDAO.find(10).getHumidityRange());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutWaterVolumeAndWateringDuration() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, , , 10-20;";

        int volume = 1;
        double duration = 2.0;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), volume, duration,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        LocalTime.of(0, 15), null, null,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(volume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(duration, mockedDAO.find(10).getWateringDuration());
        Assertions.assertEquals(LocalTime.of(0, 15), mockedDAO.find(10).getWateringInterval());
    }

    @Test
    void handleChangeWateringWithoutWaterVolumeAndHumidityRange() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, , 3, ;";

        int volume = 1;
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<>(30, 40);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), volume, 2, range),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        LocalTime.of(0, 15), null, 3.0, null));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(volume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(range, mockedDAO.find(10).getHumidityRange());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutFirstWateringWateringIntervalAndWaterVolume() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , , , 3, 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        LocalTime time = LocalTime.of(0, 1);
        int waterVolume = 1;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date, time, waterVolume, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        null,
                        null, null, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(time, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(waterVolume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(3, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutWateringIntervalWaterVolumeAndWateringDuration() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, , , , 10-20;";

        LocalTime time = LocalTime.of(0, 1);
        int waterVolume = 1;
        double duration = 2.0;
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10), time, waterVolume,
                        duration, new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        null, null, null,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(time, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(waterVolume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(duration, mockedDAO.find(10).getWateringDuration());
        Assertions.assertEquals(new AbstractMap.SimpleImmutableEntry<>(10, 20),
                mockedDAO.find(10).getHumidityRange());
    }

    @Test
    void handleChangeWateringWithoutWateringIntervalWaterVolumeAndHumidityRange() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, , , 3, ;";

        LocalTime time = LocalTime.of(0, 1);
        int waterVolume = 1;
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<>(30, 40);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        LocalDateTime.of(2017, 11, 1, 10, 10), time, waterVolume,
                        2, range),
                new ChangeWatering(new int[] {10},
                        LocalDateTime.of(2017, 12, 21, 12, 0),
                        null, null, 3.0,null));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(time, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(waterVolume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(range, mockedDAO.find(10).getHumidityRange());
        Assertions.assertEquals(3.0, mockedDAO.find(10).getWateringDuration());
    }

    @Test
    void handleChangeWateringWithoutFirstWateringWateringIntervalWaterVolumeAndWateringDuration()
            throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , , , , 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        LocalTime time = LocalTime.of(0, 1);
        int waterVolume = 1;
        double wateringDuration = 2;
        Map.Entry<Integer, Integer> changedHumidityRange = new AbstractMap.SimpleImmutableEntry<>(10, 20);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date, time, waterVolume, wateringDuration,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        null,null, null,
                        null, changedHumidityRange));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(time, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(waterVolume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(wateringDuration, mockedDAO.find(10).getWateringDuration());
        Assertions.assertEquals(changedHumidityRange, mockedDAO.find(10).getHumidityRange());
    }

    @Test
    void handleChangeWateringWithoutParams()
            throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 10, 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, , , , , ;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        LocalTime time = LocalTime.of(0, 1);
        int waterVolume = 1;
        double wateringDuration = 2;
        Map.Entry<Integer, Integer> humidityRange = new AbstractMap.SimpleImmutableEntry<>(30, 40);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{10},
                        date, time, waterVolume, wateringDuration, humidityRange),
                new ChangeWatering(new int[] {10},
                        null,null, null,
                        null, null));
        when(mockedParser.parse(input)).thenReturn(commands);

        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
        Assertions.assertEquals(time, mockedDAO.find(10).getWateringInterval());
        Assertions.assertEquals(waterVolume, mockedDAO.find(10).getWaterVolume());
        Assertions.assertEquals(wateringDuration, mockedDAO.find(10).getWateringDuration());
        Assertions.assertEquals(humidityRange, mockedDAO.find(10).getHumidityRange());
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Change watering zone 10")));
    }

    @Test
    void handleSetSensorPeriodicity() throws ParseException{
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ЗадатиПеріодичністьДатчиків: 8, 00:03;";
        LocalTime periodicity = LocalTime.of(0, 3);
        List<Command> commands = Collections.singletonList(new SetSensorPeriodicity(new int[]{8}, periodicity));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(periodicity, mockedDAO.find(8).getSensorsCheckInterval());
    }

    @Test
    void handleShowHumidity() throws ParseException{
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: 1, 2017-11-01 10:10, 00:30, 1, 2, 30-40; ПоказатиРівеньВологості: (1, 2);";
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{1},
                LocalDateTime.of(2017, 11, 1, 10, 10),
                LocalTime.of(0, 30), 1, 2,
                new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ShowHumidity(new int[]{1, 2}));
        when(mockedParser.parse(input)).thenReturn(commands);

        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Zone 1: humidity")));
        verify(mockedWindow, never()).print(argThat(s -> s.startsWith("Zone 2: humidity")));
    }

    @Test
    void handleEnableFertilizing() throws ParseException{
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиУдобрювання: 1, 5;";
        List<Command> commands = Collections.singletonList(new EnableFertilizing(new int[]{1}, 5));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);
        Assertions.assertEquals(5, mockedDAO.find(1).getFertilizerVolume());
    }

    @Test
    void handleShowFertilizing() throws ParseException{
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПоказатиУдобрювання: 1;";
        List<Command> commands = Collections.singletonList(new ShowFertilizing(new int[]{1}));
        when(mockedParser.parse(input)).thenReturn(commands);
        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Zone 1: fertilizing")));
    }

    @Test
    void handleChangeFertilizing() throws ParseException{
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ЗмінитиУдобрювання: 1, 12;";
        List<Command> commands = Collections.singletonList(new ChangeFertilizing(new int[]{1}, 12));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);
        Assertions.assertEquals(12, mockedDAO.find(1).getFertilizerVolume());
    }

    @Test
    void handleStopFertilizing() throws ParseException{
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиУдобрювання: 1, 2; ЗупинитиУдобрювання: (1, 2);";
        List<Command> commands = Arrays.asList(new EnableFertilizing(new int[] {1}, 2),
                new StopFertilizing(new int[]{1, 2}));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);
        Assertions.assertEquals(FertilizingStatus.DISABLED, mockedDAO.find(1).getFertilizingStatus());
        Assertions.assertEquals(FertilizingStatus.NOT_INITIALISED, mockedDAO.find(2).getFertilizingStatus());
    }

    @Test
    void handleMultipleCommands() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ПоказатиПолив: (5, 7, 9);\nЗупинитиПолив: (3, 7);" + "ВідновитиПолив: (7, 12);";
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{7, 8, 9, 10, 12, 15},
                        LocalDateTime.of(2017, 11, 1, 10, 10),
                        LocalTime.of(0, 30), 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ShowWatering(new int[] {5, 7, 9}),
                new StopWatering(new int[] {3, 7}),
                new ResumeWatering(new int[] {7, 12}));
        when(mockedParser.parse(input)).thenReturn(commands);

        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);

        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Zone 5: watering enabled - false")));
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Zone 7: watering enabled - true")));
        Assertions.assertNotEquals(WateringStatus.NOT_INITIALISED, mockedDAO.find(7).getWateringStatus());
    }


    @Test
    void throwErrorOnParserError() throws ParseException {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        when(mockedParser.parse(anyString())).thenThrow(ParseException.class);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        Assertions.assertThrows(ParseException.class,
                () -> app.handleCommands("ПідключитиПолив: (7-10, 12, 15), " +
                        "2017-11-01 10:10, 00:30, 1, 2, 30-40;"));

        verify(mockedDAO, times(0)).update(any());
    }
}
