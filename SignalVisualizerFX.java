import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class SignalVisualizerFX {
    private final Canvas canvas;
    private List<Short> samples;
    private double bpm;

    public SignalVisualizerFX(int width, int height) {
        this.canvas = new Canvas(width, height);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void renderWaveform(List<Short> samples, double bpm) {
        this.samples = samples;
        this.bpm = bpm;
        Platform.runLater(this::draw);
    }

    private void draw() {

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#121212"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());


        if (samples == null || samples.isEmpty()) return;

        int w = (int) canvas.getWidth();
        int h = (int) canvas.getHeight();
        int len = samples.size();
        double gain = 2.5;
        double zoom = 1;
        gc.setStroke(Color.web("#82f6ee"));
        for (int i = 1; i < len; i++) {
            int x1 = (int)((i - 1) * w / (len * zoom));
            int x2 = (int)(i * w / (len * zoom));
            int y1 = h / 2 - (int)(samples.get(i - 1) * h / 2 * gain / Short.MAX_VALUE);
            int y2 = h / 2 - (int)(samples.get(i) * h / 2 * gain / Short.MAX_VALUE);
            gc.strokeLine(x1, y1, x2, y2);
        }

        gc.setFill(Color.web("#ff7582"));
        gc.setFont(Font.font(20));
        gc.fillText("BPM: " + String.format("%.1f", bpm), 20, 40);
    }
}
