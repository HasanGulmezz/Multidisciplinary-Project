import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import java.io.File;

public final class MainApp extends Application {
    private static final int THRESHOLD = 1000;
    private static final float COOLDOWN_SEC = 0.3f;

    private final AudioFormat format = new AudioFormat(44100f, 16, 1, true, false);
    private SignalProcessor processor = null;
    private RealTimeSignalReader realtimeReader = null;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Phonocardiogram Viewer - JavaFX");

        SignalVisualizerFX visualizer = new SignalVisualizerFX(1200, 750);
        Button realtimeBtn = createStyledButton("Start Realtime", "#333333");
        Button offlineBtn = createStyledButton("Open File", "#333333");
        Button stopBtn = createStyledButton("Stop", "#aa0000");

        HBox controls = createControlBar(realtimeBtn, offlineBtn, stopBtn);
        BorderPane root = new BorderPane(visualizer.getCanvas(), null, null, controls, null);
        root.setStyle("-fx-background-color: #121212;");

        // Action bindings
        realtimeBtn.setOnAction(event -> handleRealtimeStart(visualizer));
        offlineBtn.setOnAction(event -> handleOfflineFile(stage, visualizer));
        stopBtn.setOnAction(event -> stopProcessing());

        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    private void handleRealtimeStart(SignalVisualizerFX visualizer) {
        if (processor != null) return;

        try {
            realtimeReader = new RealTimeSignalReader(format, format.getFrameSize() * 1024);
            processor = createProcessor(realtimeReader, visualizer);
            processor.startProcessingRealtime();
        } catch (Exception ex) {
            showError("Error initializing realtime reader: " + ex.getMessage());
        }
    }

    private void handleOfflineFile(Stage stage, SignalVisualizerFX visualizer) {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try {
                OfflineSignalReader reader = new OfflineSignalReader(file);
                processor = createProcessor(reader, visualizer);
                processor.startProcessingOffline();
            } catch (Exception ex) {
                showError("Error opening file: " + ex.getMessage());
            }
        }
    }

    private SignalProcessor createProcessor(SignalReader reader, SignalVisualizerFX visualizer) {
        float sampleRate = reader instanceof RealTimeSignalReader
                ? ((RealTimeSignalReader) reader).getSampleRate()
                : ((OfflineSignalReader) reader).getSampleRate();

        int cooldownSamples = (int) (COOLDOWN_SEC * sampleRate);

        return new SignalProcessor(reader, visualizer, THRESHOLD, cooldownSamples, sampleRate);
    }

    private Button createStyledButton(String label, String color) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        return btn;
    }

    private HBox createControlBar(Button... buttons) {
        HBox box = new HBox(10, buttons);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1e1e1e;");
        return box;
    }

    private void stopProcessing() {
        if (processor != null) processor.stop();
        if (realtimeReader != null) realtimeReader.close();
        processor = null;
    }

    private void showError(String msg) {
        new Alert(AlertType.ERROR, msg).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
