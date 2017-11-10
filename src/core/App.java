package core;

import command.*;
import parser.Lexer;
import parser.Parser;
import parser.RegexLexer;
import parser.TokenParser;
import ui.ConsoleUI;
import zone.*;

import java.io.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class App {
    private ZoneDAO zoneDAO;
    private Lexer lexer;
    private Parser parser;
    private HashMap<Integer, Timer> zoneTimers = new HashMap<>();
    private ConsoleUI consoleUI = new ConsoleUI();

    public static void main(String... args) {
        App app = new App();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter path of the file to be parsed:");
        String path = scanner.nextLine();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),
                "UTF-16"))) {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = reader.readLine();
            }

            String result = sb.toString();
            Collection<Command> commands = app.parser.parse(result);

            for (Command command : commands) {
                app.handleCommand(command);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public App() {
        zoneDAO = new ZoneDAOLocal();
        lexer = new RegexLexer();
        parser = new TokenParser(lexer);

        for (int i = 1; i < 16; i++) {
            zoneDAO.add(new Zone(i));
        }
    }

    private void enableWatering(EnableWatering command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setFirstWatering(command.getFirstWatering());
            zone.setWateringInterval(command.getWateringInterval());
            zone.setWaterVolume(command.getWaterVolume());
            zone.setWateringDuration(command.getWateringDuration());
            zone.setHumidityRange(command.getHumidityRange());
            zone.setWateringStatus(WateringStatus.ENABLED);
            zoneDAO.update(zone);

            setTimerForZone(zone);
            consoleUI.print("Enable watering zone " + zoneId, ConsoleUI.ANSI_BLACK);
        }
    }

    private void setTimerForZone(Zone zone) {
        int zoneId = zone.getId();
        Timer timer = new Timer(false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                consoleUI.print("Watering zone " + zoneId, ConsoleUI.ANSI_BLUE);
                if (zone.getFertilizingStatus() == FertilizingStatus.ENABLED) {
                    consoleUI.print("Fertilizing zone " + zoneId, ConsoleUI.ANSI_GREEN);
                }
            }
        };
        timer.schedule(task,
                Date.from(zone.getFirstWatering().atZone(ZoneId.systemDefault()).toInstant()),
                zone.getWateringInterval().toNanoOfDay() / 1000000);
        zoneTimers.put(zoneId, timer);
    }

    private void showWatering(ShowWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);
            String data = String.format("Zone %d: watering enabled - %b",
                    zoneId, zone.getWateringStatus() == WateringStatus.ENABLED);
            if (zone.getWateringStatus() == WateringStatus.ENABLED) {
                data = data + String.format(", first watering - %s, watering interval - %s," +
                                " water volume - %dL, watering duration - %dm, humidity range - %d%%-%d%%",
                        zone.getFirstWatering().toString(),
                        zone.getWateringInterval().toString(),
                        zone.getWaterVolume(),
                        zone.getWateringDuration(),
                        zone.getHumidityRange().getKey(),
                        zone.getHumidityRange().getValue());
            }
            data += String.format(", sensors' check interval - %s", zone.getSensorsCheckInterval());

            consoleUI.print(data, ConsoleUI.ANSI_BLACK);
        }
    }

    private void stopWatering(StopWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);
            if (zone.getWateringStatus() != WateringStatus.ENABLED) {
                continue;
            }

            zone.setWateringStatus(WateringStatus.DISABLED);
            zoneTimers.get(zoneId).cancel();
            consoleUI.print("Stop watering zone " + zoneId, ConsoleUI.ANSI_RED);
        }
    }

    private void resumeWatering(ResumeWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);
            if (zone.getWateringStatus() != WateringStatus.DISABLED) {
                continue;
            }

            zone.setWateringStatus(WateringStatus.ENABLED);
            setTimerForZone(zone);
            consoleUI.print("Resuming watering zone " + zoneId, ConsoleUI.ANSI_CYAN);
        }
    }

    private void changeWatering(ChangeWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            LocalDateTime firstWatering = command.getFirstWatering();
            if (firstWatering != null) {
                zone.setFirstWatering(firstWatering);
            }

            LocalTime wateringInterval = command.getWateringInterval();
            if (wateringInterval != null) {
                zone.setWateringInterval(wateringInterval);
            }

            Integer waterVolume = command.getWaterVolume();
            if (waterVolume != null) {
                zone.setWaterVolume(waterVolume);
            }

            Integer wateringDuration = command.getWateringDuration();
            if (wateringDuration != null) {
                zone.setWateringDuration(wateringDuration);
            }

            Map.Entry<Integer, Integer> humidityRange = command.getHumidityRange();
            if (humidityRange != null) {
                zone.setHumidityRange(humidityRange);
            }

            zoneDAO.update(zone);

            zoneTimers.get(zoneId).cancel();
            setTimerForZone(zone);
            consoleUI.print("Change watering zone " + zoneId, ConsoleUI.ANSI_BLACK);
        }
    }

    private void setSensorPeriodicity(SetSensorPeriodicity command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setSensorsCheckInterval(command.getCheckInterval());
            zoneDAO.update(zone);

            consoleUI.print("Set sensor periodicity for zone " + zoneId, ConsoleUI.ANSI_BLACK);
        }
    }

    private void showHumidity(ShowHumidity command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);
            if (zone.getWateringStatus() != WateringStatus.ENABLED) {
                continue;
            }

            int min = zone.getHumidityRange().getKey();
            int max = zone.getHumidityRange().getValue() + 1;
            String data = String.format("Zone %d: humidity - %d%%",
                    zoneId, ThreadLocalRandom.current().nextInt(min, max));
            consoleUI.print(data, ConsoleUI.ANSI_BLACK);
        }
    }

    private void enableFertilizing(EnableFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setFertilizerVolume(command.getFertilizerVolume());
            zone.setFertilizingStatus(FertilizingStatus.ENABLED);
            zoneDAO.update(zone);

            consoleUI.print("Enable fertilizing zone " + zoneId, ConsoleUI.ANSI_BLACK);
        }
    }

    private void showFertilizing(ShowFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            String data = String.format("Zone %d: fertilizing enabled - %b, fertilizer volume - %dL",
                    zoneId, zone.getFertilizingStatus() == FertilizingStatus.ENABLED, zone.getFertilizerVolume());
            consoleUI.print(data, ConsoleUI.ANSI_BLACK);
        }
    }

    private void changeFertilizing(ChangeFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setFertilizerVolume(command.getFertilizerVolume());
            zoneDAO.update(zone);

            consoleUI.print("Change fertilizing zone " + zoneId, ConsoleUI.ANSI_BLACK);
        }
    }

    private void stopFertilizing(StopFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            if (zone.getFertilizingStatus() != FertilizingStatus.ENABLED) {
                continue;
            }

            zone.setFertilizingStatus(FertilizingStatus.DISABLED);
            consoleUI.print("Stop fertilizing zone " + zoneId, ConsoleUI.ANSI_RED);
        }
    }

    private void handleCommand(Command command) {
        switch (command.getName()) {
            case EnableWatering.NAME:
                enableWatering((EnableWatering) command);
                break;
            case ShowWatering.NAME:
                showWatering((ShowWatering) command);
                break;
            case StopWatering.NAME:
                stopWatering((StopWatering) command);
                break;
            case ResumeWatering.NAME:
                resumeWatering((ResumeWatering) command);
                break;
            case ChangeWatering.NAME:
                changeWatering((ChangeWatering) command);
                break;
            case SetSensorPeriodicity.NAME:
                setSensorPeriodicity((SetSensorPeriodicity) command);
                break;
            case ShowHumidity.NAME:
                showHumidity((ShowHumidity) command);
                break;
            case EnableFertilizing.NAME:
                enableFertilizing((EnableFertilizing) command);
                break;
            case ShowFertilizing.NAME:
                showFertilizing((ShowFertilizing) command);
                break;
            case ChangeFertilizing.NAME:
                changeFertilizing((ChangeFertilizing) command);
                break;
            case StopFertilizing.NAME:
                stopFertilizing((StopFertilizing) command);
                break;
        }
    }
}
