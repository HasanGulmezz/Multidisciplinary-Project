import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import java.io.File;

public class MainApp extends Application {
    private static final int THRESHOLD = 1000;
    private static final float COOLDOWN_SEC = 0.3f;
    private SignalProcessor processor;
    private RealTimeSignalReader realtimeReader;
    private SignalVisualizerFX visualizer;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Phonocardiogram Viewer - JavaFX");

        visualizer = new SignalVisualizerFX(1200, 750);
        Button realtimeBtn = new Button("Start Realtime");
        Button offlineBtn = new Button("Open File");
        Button stopBtn = new Button("Stop");
        realtimeBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        offlineBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        stopBtn.setStyle("-fx-background-color: #aa0000; -fx-text-fill: white;");


        HBox controls = new HBox(10, realtimeBtn, offlineBtn, stopBtn);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: #1e1e1e;");
        controls.setAlignment(Pos.CENTER);
        controls.setPrefWidth(800);
        BorderPane root = new BorderPane(visualizer.getCanvas(), null, null, controls, null);
        root.setStyle("-fx-background-color: #121212;");


        AudioFormat format = new AudioFormat(44100f, 16, 1, true, false);

        realtimeBtn.setOnAction(e -> {
            if (processor != null) return;
            try {
                realtimeReader = new RealTimeSignalReader(format, format.getFrameSize() * 1024);
                float sampleRate = realtimeReader.getSampleRate();
                int cooldownSamples = (int)(COOLDOWN_SEC * sampleRate);
                processor = new SignalProcessor(realtimeReader, visualizer, THRESHOLD, cooldownSamples, sampleRate);
                processor.startProcessingRealtime();
            } catch (Exception ex) {
                showAlert("Error initializing realtime reader: " + ex.getMessage());
            }
        });

        offlineBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    OfflineSignalReader reader = new OfflineSignalReader(file);
                    float sampleRate = reader.getSampleRate();
                    int cooldownSamples = (int)(COOLDOWN_SEC * sampleRate);
                    processor = new SignalProcessor(reader, visualizer, THRESHOLD, cooldownSamples, sampleRate);
                    processor.startProcessingOffline();
                } catch (Exception ex) {
                    showAlert("Error opening file: " + ex.getMessage());
                }
            }
        });

        stopBtn.setOnAction(e -> {
            if (processor != null) processor.stop();
            if (realtimeReader != null) realtimeReader.close();
            processor = null;
        });

        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
