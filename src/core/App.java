package core;

import command.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import parser.Parser;
import parser.RegexLexer;
import parser.TokenParser;
import ui.MainWindow;
import zone.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class App extends Application{
    private static final int AMOUNT_OF_ZONES = 15;
    private static final Logger sensorLogger = LogManager.getLogger("sensor");

    private ZoneDAO zoneDAO;
    private Parser parser;
    private HashMap<Integer, Timer> zoneWateringTimers = new HashMap<>();
    private HashMap<Integer, Timer> zoneSensorsTimers = new HashMap<>();
    private MainWindow mainWindowController;

    public static void main(String... args) {
        Application.launch();
    }

    public App() {
        this(new ZoneDAOLocal(), new TokenParser(new RegexLexer()));
    }

    public App(ZoneDAO zoneDAO, Parser parser) {
        this.zoneDAO = zoneDAO;
        this.parser = parser;

        for (int i = 1; i < AMOUNT_OF_ZONES + 1; i++) {
            zoneDAO.add(new Zone(i));
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_window.fxml"));
        AnchorPane root = loader.load();

        mainWindowController = loader.getController();
        mainWindowController.setApp(this);
        mainWindowController.initGridPane(root);

        Scene scene = new Scene(root, 725, 500);
        primaryStage.setTitle("Irrigation");
        primaryStage.setScene(scene);
        primaryStage.show();

        for (int i = 1; i < AMOUNT_OF_ZONES + 1; i++) {
            setSensorsTimersForZone(zoneDAO.find(i));
        }
    }

    public void handleCommands(String input) throws ParseException {
        Collection<Command> commands = parser.parse(input);
        for (Command command : commands) {
            handleCommand(command);
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

            setWateringTimersForZone(zone);
            mainWindowController.print("Enable watering zone " + zoneId);

        }
    }

    private void setWateringTimersForZone(Zone zone) {
        int zoneId = zone.getId();
        Timer timer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(zone.isWaterSensorNotResponding()) {
                    mainWindowController.print("Zone " + zoneId +
                            "'s water sensor is not responding! Please check it!");
                }
                mainWindowController.print("Watering zone " + zoneId);
                mainWindowController.changeZoneColor(zoneId, Color.GREEN);

                if (zone.getFertilizingStatus() == FertilizingStatus.ENABLED) {
                    if(zone.isFertilizerSensorNotResponding()) {
                        mainWindowController.print("Zone " + zoneId +
                                "'s fertilizing sensor is not responding! Please check it!");
                    }
                    mainWindowController.print("Fertilizing zone " + zoneId);
                    mainWindowController.changeZoneBorderSize(zoneId, 5.0);
                }

                int delay = (int) (zone.getWateringDuration() * 60000);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mainWindowController.print("Watering zone stopped: " + zoneId);
                        mainWindowController.changeZoneColor(zoneId, Color.BLACK);
                        mainWindowController.changeZoneBorderSize(zoneId, 0);
                    }
                }, delay);
            }
        };
        timer.schedule(task,
                Date.from(zone.getFirstWatering().atZone(ZoneId.systemDefault()).toInstant()),
                zone.getWateringInterval().toNanoOfDay() / 1000000);
        zoneWateringTimers.put(zoneId, timer);
    }

    private void setSensorsTimersForZone(Zone zone) {
        int zoneId = zone.getId();
        Timer timer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int humidity = zone.getHumidityValue();
                sensorLogger.info(zone.getId() + " " + humidity);
                mainWindowController.print(String.format("Zone %d: humidity - %d", zoneId, humidity));
            }
        };

        timer.schedule(task, Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()),
                zone.getSensorsCheckInterval().toNanoOfDay() / 1000000);
        zoneSensorsTimers.put(zoneId, timer);
    }

    private void showWatering(ShowWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);
            String data = String.format("Zone %d: watering enabled - %b",
                    zoneId, zone.getWateringStatus() == WateringStatus.ENABLED);
            if (zone.getWateringStatus() == WateringStatus.ENABLED) {
                data = data + String.format(", first watering - %s, watering interval - %s," +
                                " water volume - %dL, watering duration - %fm, humidity range - %d%%-%d%%",
                        zone.getFirstWatering().toString(),
                        zone.getWateringInterval().toString(),
                        zone.getWaterVolume(),
                        zone.getWateringDuration(),
                        zone.getHumidityRange().getKey(),
                        zone.getHumidityRange().getValue());
            }
            data += String.format(", sensors' check interval - %s", zone.getSensorsCheckInterval());

            mainWindowController.print(data);
        }
    }

    private void stopWatering(StopWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            if (zone.getWateringStatus() != WateringStatus.ENABLED) {
                continue;
            }

            zone.setWateringStatus(WateringStatus.DISABLED);
            zoneDAO.update(zone);
            zoneWateringTimers.get(zoneId).cancel();
            mainWindowController.print("Stop watering zone " + zoneId);
        }
    }

    private void resumeWatering(ResumeWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);
            if (zone.getWateringStatus() != WateringStatus.DISABLED) {
                continue;
            }
            zone.setWateringStatus(WateringStatus.ENABLED);
            zoneDAO.update(zone);
            setWateringTimersForZone(zone);
            mainWindowController.print("Resuming watering zone " + zoneId);
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

            Double wateringDuration = command.getWateringDuration();
            if (wateringDuration != null) {
                zone.setWateringDuration(wateringDuration);
            }

            Map.Entry<Integer, Integer> humidityRange = command.getHumidityRange();
            if (humidityRange != null) {
                zone.setHumidityRange(humidityRange);
            }

            zoneDAO.update(zone);

            zoneWateringTimers.get(zoneId).cancel();
            setWateringTimersForZone(zone);
            mainWindowController.print("Change watering zone " + zoneId);
        }
    }

    private void setSensorPeriodicity(SetSensorPeriodicity command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setSensorsCheckInterval(command.getCheckInterval());
            zoneDAO.update(zone);
            zoneSensorsTimers.get(zoneId).cancel();
            setSensorsTimersForZone(zone);

            mainWindowController.print("Set sensor periodicity for zone " + zoneId);
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
            mainWindowController.print(data);
        }
    }

    private void enableFertilizing(EnableFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setFertilizerVolume(command.getFertilizerVolume());
            zone.setFertilizingStatus(FertilizingStatus.ENABLED);
            zoneDAO.update(zone);

            mainWindowController.print("Enable fertilizing zone " + zoneId);
        }
    }

    private void showFertilizing(ShowFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            String data = String.format("Zone %d: fertilizing enabled - %b, fertilizer volume - %dL",
                    zoneId, zone.getFertilizingStatus() == FertilizingStatus.ENABLED, zone.getFertilizerVolume());
            mainWindowController.print(data);
        }
    }

    private void changeFertilizing(ChangeFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setFertilizerVolume(command.getFertilizerVolume());
            zoneDAO.update(zone);

            mainWindowController.print("Change fertilizing zone " + zoneId);
        }
    }

    private void stopFertilizing(StopFertilizing command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            if (zone.getFertilizingStatus() != FertilizingStatus.ENABLED) {
                continue;
            }

            zone.setFertilizingStatus(FertilizingStatus.DISABLED);
            mainWindowController.print("Stop fertilizing zone " + zoneId);
        }
    }

    public void resetAllZones(){
        for(int i : zoneWateringTimers.keySet()){
            resetZoneState(i);
            mainWindowController.showLines(i);
        }

        for(Timer sensorsTimer : zoneSensorsTimers.values()) {
            sensorsTimer.cancel();
        }

        mainWindowController.print("System urgently stopped");
    }

    public void waterShortage() {
        for(int i : zoneWateringTimers.keySet()){
            resetZoneState(i);
        }
        mainWindowController.print("Water shortage! Please refill the water tank");
    }

    public void fertilizerShortage() {
        for(Zone zone : zoneDAO.findAll())
        {
            zone.setFertilizingStatus(FertilizingStatus.DISABLED);
            mainWindowController.changeZoneBorderSize(zone.getId(), 0);
        }
        mainWindowController.print("Fertilizer shortage! Please refill the fertilizer tank");
    }

    public void invalidHumidity(){
        Zone zone = zoneDAO.find(7);
        zone.setHumidityValue(50);
        resetZoneState(7);
    }

    public void resumeWateringButton(){
       int[] zones = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
       for(int i : zones){
           mainWindowController.hideLines(i);
       }

       resumeWatering(new ResumeWatering(zones));
    }

    private void resetZoneState(int id) {
        zoneDAO.find(id).setWateringStatus(WateringStatus.DISABLED);
        zoneWateringTimers.get(id).cancel();
        mainWindowController.changeZoneColor(id, Color.BLACK);
        mainWindowController.changeZoneBorderSize(id, 0);
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

    public void setMainWindowController(MainWindow mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    public void waterSensorNotResponding() {
        zoneDAO.find(9).setWaterSensorNotResponding(true);
    }

    public void fertilizerSensorNotResponding() {
        zoneDAO.find(9).setFertilizerSensorNotResponding(true);
    }
}
