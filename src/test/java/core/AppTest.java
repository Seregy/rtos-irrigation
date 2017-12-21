package core;

import command.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import ui.MainWindow;
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

        App app = new App(mockedDAO, mockedParser);
        app.setMainWindowController(mock(MainWindow.class));
        app.handleCommands(input);

        verify(mockedDAO, times(6)).find(anyInt());
        verify(mockedDAO, times(6)).update(any());
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

        App app = new App(mockedDAO, mockedParser);
        app.setMainWindowController(mock(MainWindow.class));
        app.handleCommands(input);

        verify(mockedDAO, times(13)).find(anyInt());
        verify(mockedDAO, times(8)).update(any());
    }

    @Test
    void throwErrorOnParserError() throws ParseException {
        ZoneDAO mockedDAO = mockZoneDAO();
        Parser mockedParser = mock(Parser.class);

        when(mockedParser.parse(anyString())).thenThrow(ParseException.class);

        App app = new App(mockedDAO, mockedParser);
        Assertions.assertThrows(ParseException.class,
                () -> app.handleCommands("ПідключитиПолив: (7-10, 12, 15), " +
                        "2017-11-01 10:10, 00:30, 1, 2, 30-40;"));

        verify(mockedDAO, times(0)).find(anyInt());
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
