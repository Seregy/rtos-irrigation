package core;

import command.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
    private Circle [] zones = new Circle[AMOUNT_OF_ZONES];

    private ZoneDAO zoneDAO;
    private Parser parser;
    private HashMap<Integer, Timer> zoneTimers = new HashMap<>();
    private MainWindow mainWindowController;
    private GridPane gp = new GridPane();
    private Circle[][] circlesArray = new Circle[3][5];

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

    public static GridPane initZonesArray(Circle[][] circlesMatrix, GridPane pane) {
        for(int i = 0; i < circlesMatrix.length; i++) {
            ColumnConstraints column = new ColumnConstraints(90);
            pane.getColumnConstraints().add(column);
            for(int j = 0; j < circlesMatrix[i].length; j++) {
                RowConstraints row = new RowConstraints(70);
                pane.getRowConstraints().add(row);
                circlesMatrix[i][j] = new Circle(20);
                pane.add(circlesMatrix[i][j], i, j);
            }
        }
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_window.fxml"));
        AnchorPane root = loader.load();

        mainWindowController = loader.getController();
        mainWindowController.setApp(this);

        gp.setLayoutX(14.0);
        gp.setLayoutY(14.0);
        gp.setPrefHeight(321.0);
        gp.setPrefWidth(236.0);
        GridPane newGridPane = initZonesArray(circlesArray, gp);

        root.getChildren().add(newGridPane);
        Scene scene = new Scene(root, 725, 500);
        primaryStage.setTitle("Irrigation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void handleCommands(String input) throws ParseException {
        Collection<Command> commands = parser.parse(input);
        for (Command command : commands) {
            handleCommand(command);
        }
    }

    public static void changeColorNodeByRowColumnIndex (final int row, final int column, GridPane gridPane, Paint color) {
        ObservableList<Node> childrens = gridPane.getChildren();
        Circle circle = null;
        for (Node node : childrens) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                circle = (Circle) node;
                circle.setFill(color);
                childrens.remove(node);
                childrens.add(circle);
                break;
            }
        }
    }

    public static void changeStrokeNodeByRowColumnIndex (final int row, final int column, GridPane gridPane, double width) {
        ObservableList<Node> childrens = gridPane.getChildren();
        Circle circle = null;
        for (Node node : childrens) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                circle = (Circle) node;
                circle.setStroke(Color.ORANGE);
                circle.setStrokeWidth(width);
                childrens.remove(node);
                childrens.add(circle);
                break;
            }
        }
    }

    public static Map<String, Integer> getIndex(int zoneNumber) {
        int row = Math.floorDiv(zoneNumber - 1, 3);
        int column = (zoneNumber - 1) % 3;

        Map<String, Integer> map = new HashMap<>();
        map.put("row", row);
        map.put("column", column);

        return map;
    }

    private void enableWatering(EnableWatering command) {
        for(int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            // Get current index
            Map<String, Integer> map = getIndex(zoneId);
            // Change color
            changeColorNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, Color.GREEN);

            zone.setFirstWatering(command.getFirstWatering());
            zone.setWateringInterval(command.getWateringInterval());
            zone.setWaterVolume(command.getWaterVolume());
            zone.setWateringDuration(command.getWateringDuration());
            zone.setHumidityRange(command.getHumidityRange());
            zone.setWateringStatus(WateringStatus.ENABLED);
            zoneDAO.update(zone);

            setTimerForZone(zone);
            mainWindowController.print("Enable watering zone " + zoneId);

        }
    }

    private void setTimerForZone(Zone zone) {
        int zoneId = zone.getId();
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Map<String, Integer> map = getIndex(zoneId);
                Platform.runLater(() -> {
                    changeColorNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, Color.GREEN);
                });
                mainWindowController.print("Watering zone " + zoneId);

                if (zone.getFertilizingStatus() == FertilizingStatus.ENABLED) {
                    mainWindowController.print("Fertilizing zone " + zoneId);
                    Platform.runLater(() -> {
                        changeStrokeNodeByRowColumnIndex(map.get("row"), map.get("column"), gp,5.0);
                    });
                }

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Map<String, Integer> map = getIndex(zoneId);
                        Platform.runLater(() -> {
                            changeColorNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, Color.BLACK);
                            changeStrokeNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, 0);
                        });

                        mainWindowController.print("Watering zone stopped: " + zoneId);
                    }
                }, zone.getWateringDuration() * 60000);
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

            mainWindowController.print(data);
        }
    }

    private void stopWatering(StopWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            if (zone.getWateringStatus() != WateringStatus.ENABLED) {
                continue;
            }

            // Get current index
            Map<String, Integer> map = getIndex(zoneId);
            // Change color
            changeColorNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, Color.BLACK);

            zone.setWateringStatus(WateringStatus.DISABLED);
            zoneDAO.update(zone);
            zoneTimers.get(zoneId).cancel();
            mainWindowController.print("Stop watering zone " + zoneId);
        }

    }

    private void resumeWatering(ResumeWatering command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);
            if (zone.getWateringStatus() != WateringStatus.DISABLED) {
                continue;
            }

            // Get current index
            Map<String, Integer> map = getIndex(zoneId);
            // Change color
            changeColorNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, Color.GREEN);

            zone.setWateringStatus(WateringStatus.ENABLED);
            zoneDAO.update(zone);
            setTimerForZone(zone);
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
            mainWindowController.print("Change watering zone " + zoneId);
        }
    }

    private void setSensorPeriodicity(SetSensorPeriodicity command) {
        for (int zoneId : command.getZones()) {
            Zone zone = zoneDAO.find(zoneId);

            zone.setSensorsCheckInterval(command.getCheckInterval());
            zoneDAO.update(zone);

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

            // Get current index
            Map<String, Integer> map = getIndex(zoneId);
            // Change color
            changeStrokeNodeByRowColumnIndex(map.get("row"), map.get("column"), gp,5.0);

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

            // Get current index
            Map<String, Integer> map = getIndex(zoneId);
            // Change color
            changeStrokeNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, 0);

            zone.setFertilizingStatus(FertilizingStatus.DISABLED);
            mainWindowController.print("Stop fertilizing zone " + zoneId);
        }
    }

    public void stopEverything(){
        for(int i : zoneTimers.keySet()){
            stopZoneWork(i);
        }

        mainWindowController.print("System urgently stopped");
    }

    public void invalidHumidity(){
        Zone zone = zoneDAO.find(7);
        zone.setHumidityValue(50);
        stopZoneWork(7);
    }

    private void stopZoneWork(int id) {
        zoneTimers.get(id).cancel();
        Map<String, Integer> map = getIndex(id);
        changeColorNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, Color.BLACK);
        changeStrokeNodeByRowColumnIndex(map.get("row"), map.get("column"), gp, 0);
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
