package ui;

import core.App;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.image.Image;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.*;

public class MainWindow {
    private static final Logger interruptLogger = LogManager.getLogger("interrupt");
    private static final Logger commandLogger = LogManager.getLogger("command");
    private static final Logger generalLogger = LogManager.getLogger("error");

    private App app;
    @FXML
    private Button commandButton;
    @FXML
    private TextField commandField;
    @FXML
    private TextArea textArea;
    @FXML
    private GridPane pane;
    @FXML
    private GridPane linePane;

    private Circle[][] circlesArray = new Circle[3][5];
    private Map<Integer, Line[]> linesDictionary = new HashMap<>();

    @FXML
    protected void handleSendCommand(ActionEvent actionEvent) {
        try {
            app.handleCommands(commandField.getText());
            commandLogger.info(commandField.getText());
            commandField.clear();
        } catch (ParseException e) {
            generalLogger.error(e.getMessage());
        }
    }

    @FXML
    protected void handleResumeCommand(ActionEvent actionEvent) {
        app.resumeWateringButton();
    }

    @FXML
    protected void handleStopCommand(ActionEvent actionEvent) {
        interruptLogger.info("1");
        app.resetAllZones();
    }

    @FXML
    protected void handleInvalidHumidityCommand(ActionEvent actionEvent){
        interruptLogger.info("2");
        app.invalidHumidity();
    }

    @FXML
    protected void handleWaterShortageCommand(ActionEvent actionEvent){
        interruptLogger.info("3");
        app.waterShortage();
    }

    @FXML
    protected void handleFertilizerShortageCommand(ActionEvent actionEvent){
        interruptLogger.info("4");
        app.fertilizerShortage();
    }

    @FXML
    protected void handleWaterNoResponseCommand(ActionEvent actionEvent){
        interruptLogger.info("5");
        app.waterSensorNotResponding();
    }

    @FXML
    protected void handleFertilizerNoResponseCommand(ActionEvent actionEvent){
        interruptLogger.info("6");
        app.fertilizerSensorNotResponding();
    }

    @FXML
    protected void handleShowLogsCommand(ActionEvent actionEvent){
        File file = new File("logs//general.log");
        if (file.exists())
        {
            if (Desktop.isDesktopSupported())
            {
                try
                {
                    Desktop.getDesktop().open(file);
                }
                catch (IOException e)
                {

                    e.printStackTrace();
                }
            }
            else
            {
                System.out.println("Awt Desktop is not supported!");
            }
        }
    }

    @FXML
    protected void handleLoadProgrammeCommand(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Program File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("txt", "*.txt")
        );
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            openFile(file);
        }
    }

    private void openFile(File file){
        try {
            Scanner input = new Scanner(file);
            while (input.hasNext()) {
                String string = input.nextLine();
                app.handleCommands(string);
                commandLogger.info(string);
            }
            input.close();
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    public void setApp(App app) {
        this.app = app;
    }

    public void print(String stringToPrint) {
        Platform.runLater(() -> {
            LocalTime time = LocalTime.now();
            textArea.appendText("[" + time.toString() + "] " + stringToPrint + System.lineSeparator());
        });
    }

    public void initGridPane(AnchorPane rootPane) {
        pane = new GridPane();
        pane.setLayoutX(14.0);
        pane.setLayoutY(14.0);
        pane.setPrefHeight(321.0);
        pane.setPrefWidth(236.0);
        linePane = new GridPane();
        linePane.setLayoutX(14.0);
        linePane.setLayoutY(14.0);
        linePane.setPrefHeight(321.0);
        linePane.setPrefWidth(236.0);
        initZonesArray(circlesArray);
        rootPane.getChildren().add(pane);
        rootPane.getChildren().add(linePane);
        TextAreaAppender.setTextArea(textArea);
    }

    public void changeZoneColor(int zoneId, Color color) {
        Map<String, Integer> map = getIndex(zoneId);
        Platform.runLater(() -> changeColorNodeByRowColumnIndex(map.get("row"), map.get("column"), color));
    }

    public void changeZoneBorderSize(int zoneId, double size) {
        Map<String, Integer> map = getIndex(zoneId);
        Platform.runLater(() -> changeStrokeNodeByRowColumnIndex(map.get("row"), map.get("column"), size));
    }

    private void initZonesArray(Circle[][] circlesMatrix) {
        for(int i = 0; i < circlesMatrix.length; i++) {
            ColumnConstraints column = new ColumnConstraints(90);
            pane.getColumnConstraints().add(column);
            linePane.getColumnConstraints().add(column);
            for(int j = 0; j < circlesMatrix[i].length; j++) {
                RowConstraints row = new RowConstraints(70);
                pane.getRowConstraints().add(row);
                linePane.getRowConstraints().add(row);
                circlesMatrix[i][j] = new Circle(20);
                pane.add(circlesMatrix[i][j], i, j);
                drawLines(i, j);
            }
        }
    }

    private void drawLines(int i, int j){
        double x = 0;
        double y = 40;
        double s = 2;
        Line line1 = new Line(x+s, x+s,y-s, y-s);
        line1.setStroke(Color.RED);
        line1.setStrokeWidth(2);
        line1.setVisible(false);
        Line line2 = new Line(x+s, x+s,y-s, y-s);
        line2.setRotate(90);
        line2.setStroke(Color.RED);
        line2.setStrokeWidth(2);
        line2.setVisible(false);
        linesDictionary.put(j*3+i+1, new Line[]{line1,line2});
        linePane.add(line1, i, j);
        linePane.add(line2, i, j);
    }

    public void showLines(int index){
        Line[] lines = linesDictionary.get(index);
        for (Line line: lines) {
            line.setVisible(true);
        }
    }

    public void hideLines(int index){
        Line[] lines = linesDictionary.get(index);
        for (Line line: lines) {
            line.setVisible(false);
        }
    }

    private static Map<String, Integer> getIndex(int zoneNumber) {
        int row = Math.floorDiv(zoneNumber - 1, 3);
        int column = (zoneNumber - 1) % 3;

        Map<String, Integer> map = new HashMap<>();
        map.put("row", row);
        map.put("column", column);

        return map;
    }

    private void changeColorNodeByRowColumnIndex (final int row, final int column, Paint color) {
        ObservableList<Node> childrens = pane.getChildren();
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

    private void changeStrokeNodeByRowColumnIndex (final int row, final int column, double width) {
        ObservableList<Node> childrens = pane.getChildren();
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
}
