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

public class AppTest {
    @Test
    void handleOneCommand() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;";
        List<Command> commands = Collections.singletonList(new EnableWatering(new int[]{7, 8, 9, 10, 12, 15},
                LocalDateTime.of(2017, 11, 1, 10, 10),
                LocalTime.of(0, 30), 1, 2,
                new AbstractMap.SimpleImmutableEntry<>(30, 40)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertNotEquals(WateringStatus.NOT_INITIALISED, mockedDAO.find(7).getWateringStatus());
    }

    @Test
    void handleMultipleCommands() throws ParseException {
        ZoneDAO mockedDAO = mockZoneDAO();
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

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertNotEquals(WateringStatus.NOT_INITIALISED, mockedDAO.find(7).getWateringStatus());
    }


    @Test
    void throwErrorOnParserError() throws ParseException {
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        when(mockedParser.parse(anyString())).thenThrow(ParseException.class);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        Assertions.assertThrows(ParseException.class,
                () -> app.handleCommands("ПідключитиПолив: (7-10, 12, 15), " +
                        "2017-11-01 10:10, 00:30, 1, 2, 30-40;"));

        verify(mockedDAO, times(0)).update(any());
    }

    @Test
    void handleChangeWatering() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: (7-10, 12, 15), 2017-11-01 10:10, 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, 2, 3, 10-20;";

        LocalTime interval = LocalTime.of(0, 15);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{7, 8, 9, 10, 12, 15},
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
    void handleChangeWateringWithoutOneParam() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: (7-10, 12, 15), , 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, 2, 3, 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{7, 8, 9, 10, 12, 15},
                        date,
                        LocalTime.of(0, 30), 1, 2,
                        new AbstractMap.SimpleImmutableEntry<>(30, 40)),
                new ChangeWatering(new int[] {10},
                        null,
                        LocalTime.of(0, 30), 2, 3.0,
                        new AbstractMap.SimpleImmutableEntry<>(10, 20)));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);

        Assertions.assertEquals(date, mockedDAO.find(10).getFirstWatering());
    }

    @Test
    void handleChangeWateringWithoutTwoParam() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиПолив: (7-10, 12, 15), , 00:30, 1, 2, 30-40;" +
                "ЗмінитиПолив: 10, 2017-12-21 12:00, 00:15, 2, 3, 10-20;";

        LocalDateTime date = LocalDateTime.of(2017, 11, 1, 10, 10);
        LocalTime time = LocalTime.of(0, 1);
        List<Command> commands = Arrays.asList(new EnableWatering(new int[]{7, 8, 9, 10, 12, 15},
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
    }

    @Test
    void handleSetSensorPeriodicityCommand() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
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
    void handleShowHumidityCommand() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
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
        verify(mockedDAO, times(3)).update(mockedDAO.find(1));
        verify(mockedWindow, atLeastOnce()).print(anyString());
    }

    @Test
    void handleEnableFertilizingCommand() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПідключитиУдобрювання: 1, 5;";
        List<Command> commands = Collections.singletonList(new EnableFertilizing(new int[]{1}, 5));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);
        Assertions.assertEquals(5, mockedDAO.find(1).getFertilizerVolume());
    }

    @Test
    void handleShowFertilizingCommand() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ПоказатиУдобрювання: 1;";
        List<Command> commands = Collections.singletonList(new ShowFertilizing(new int[]{1}));
        when(mockedParser.parse(input)).thenReturn(commands);
        MainWindow mockedWindow = mock(MainWindow.class);
        App app = new App(mockedDAO, mockedParser, mockedWindow);
        app.handleCommands(input);
        verify(mockedWindow, atLeastOnce()).print(anyString());
    }

    @Test
    void handleChangeFertilizingCommand() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        String input = "ЗмінитиУдобрювання: 1, 12;";
        List<Command> commands = Collections.singletonList(new ChangeFertilizing(new int[]{1}, 12));
        when(mockedParser.parse(input)).thenReturn(commands);

        App app = new App(mockedDAO, mockedParser, mock(MainWindow.class));
        app.handleCommands(input);
        Assertions.assertEquals(12, mockedDAO.find(1).getFertilizerVolume());
    }

    @Test
    void handleStopFertilizingCommand() throws ParseException{
        ZoneDAO mockedDAO = mockZoneDAO();
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

    private ZoneDAO mockZoneDAO() {
        ZoneDAO mockedDAO = mock(ZoneDAO.class);
        List<Zone> zones = new ArrayList<>();

        when(mockedDAO.find(anyInt()))
                .thenAnswer(invocation -> zones.get((int) invocation.getArgument(0) - 1));
        when(mockedDAO.update(any()))
                .thenReturn(true);
        when(mockedDAO.add(any()))
                .thenAnswer(invocation -> zones.add(invocation.getArgument(0)));
        when(mockedDAO.delete(anyInt()))
                .thenAnswer(invocation -> (zones.remove(((int) invocation.getArgument(0)) - 1) != null));
        when(mockedDAO.findAll())
                .thenReturn(zones);

        return mockedDAO;
    }
}
