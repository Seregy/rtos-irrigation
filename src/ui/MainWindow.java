package ui;

import core.App;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

import java.text.ParseException;
import java.time.LocalTime;

public class MainWindow {
    private Circle [][] zones = new Circle[3][5];

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
    protected void handleSendCommand(ActionEvent actionEvent) {
        try {
            app.handleCommands(commandField.getText());
        } catch (ParseException e) {
            Platform.runLater(() -> textArea.appendText(e.getMessage()));
        }
    }

    @FXML
    protected void handleStopCommand(ActionEvent actionEvent) {
        app.stopEverything();
    }

    @FXML
    protected void handleInvalidHumidityCommand(ActionEvent actionEvent){
        app.invalidHumidity();
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

}
