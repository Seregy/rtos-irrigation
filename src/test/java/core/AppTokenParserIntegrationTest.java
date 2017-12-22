package core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.RegexLexer;
import parser.TokenParser;
import ui.MainWindow;
import zone.FertilizingStatus;
import zone.WateringStatus;
import zone.ZoneDAO;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class AppTokenParserIntegrationTest {
    @Test
    void handleMultipleCommands() throws Exception {
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        App app = new App(mockedDAO, new TokenParser(new RegexLexer()), mock(MainWindow.class));

        app.handleCommands("ПідключитиПолив: (7-10, 12, 15), 2017-11-10 16:17, 00:00:15, 1, 0.1, 30-40;\n" +
                "ЗупинитиПолив: (10-15);");

        Assertions.assertEquals(WateringStatus.NOT_INITIALISED, mockedDAO.find(5).getWateringStatus());
        Assertions.assertEquals(WateringStatus.ENABLED, mockedDAO.find(7).getWateringStatus());
        Assertions.assertEquals(WateringStatus.DISABLED, mockedDAO.find(10).getWateringStatus());
    }

    @Test
    void handleResumeWateringButton() throws Exception {
        MainWindow mockedWindow = mock(MainWindow.class);
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        App app = new App(mockedDAO, new TokenParser(new RegexLexer()), mockedWindow);

        app.handleCommands("ПідключитиПолив: 7, 2017-11-10 16:17, 00:00:15, 1, 0.1, 30-40;\n" +
                "ЗупинитиПолив: 7;");
        app.resumeWateringButton();

        verify(mockedWindow, times(15)).hideLines(anyInt());
        Assertions.assertEquals(WateringStatus.ENABLED, mockedDAO.find(7).getWateringStatus());
    }

    @Test
    void handleResetAllZones() throws Exception {
        MainWindow mockedWindow = mock(MainWindow.class);
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        App app = new App(mockedDAO, new TokenParser(new RegexLexer()), mockedWindow);

        app.handleCommands("ПідключитиПолив: 7, 2017-11-10 16:17, 00:00:15, 1, 0.1, 30-40;");
        app.resetAllZones();

        Assertions.assertEquals(WateringStatus.DISABLED, mockedDAO.find(7).getWateringStatus());
        verify(mockedWindow, times(1)).showLines(anyInt());
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("System urgently stopped")));
    }

    @Test
    void handleWaterShortage() throws Exception {
        MainWindow mockedWindow = mock(MainWindow.class);
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        App app = new App(mockedDAO, new TokenParser(new RegexLexer()), mockedWindow);

        app.handleCommands("ПідключитиПолив: 7, 2017-11-10 16:17, 00:00:15, 1, 0.1, 30-40;");
        app.waterShortage();

        Assertions.assertEquals(WateringStatus.DISABLED, mockedDAO.find(7).getWateringStatus());
        verify(mockedWindow, times(1)).showLines(anyInt());
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Water shortage!")));
    }

    @Test
    void handleFertilizerShortage() throws Exception {
        MainWindow mockedWindow = mock(MainWindow.class);
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        App app = new App(mockedDAO, new TokenParser(new RegexLexer()), mockedWindow);

        app.handleCommands("ПідключитиПолив: 7, 2017-11-10 16:17, 00:00:15, 1, 0.1, 30-40;" +
                "ПідключитиУдобрювання: 7, 1;");
        app.fertilizerShortage();

        Assertions.assertEquals(FertilizingStatus.DISABLED, mockedDAO.find(7).getFertilizingStatus());
        verify(mockedWindow, times(1)).print(argThat(s -> s.startsWith("Fertilizer shortage!")));
    }

    @Test
    void handleWaterSensorNotResponding() throws Exception {
        MainWindow mockedWindow = mock(MainWindow.class);
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        App app = new App(mockedDAO, new TokenParser(new RegexLexer()), mockedWindow);

        app.waterSensorNotResponding(1);

        Assertions.assertTrue(mockedDAO.find(1).isWaterSensorNotResponding());
        Assertions.assertFalse(mockedDAO.find(2).isWaterSensorNotResponding());
    }

    @Test
    void handleFertilizerSensorNotResponding() throws Exception {
        MainWindow mockedWindow = mock(MainWindow.class);
        ZoneDAO mockedDAO = TestUtils.mockZoneDAO();
        App app = new App(mockedDAO, new TokenParser(new RegexLexer()), mockedWindow);

        app.fertilizerSensorNotResponding(1);

        Assertions.assertTrue(mockedDAO.find(1).isFertilizerSensorNotResponding());
        Assertions.assertFalse(mockedDAO.find(2).isFertilizerSensorNotResponding());
    }
}
