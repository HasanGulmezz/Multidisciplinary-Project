import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;
import java.util.stream.IntStream;

public final class SignalVisualizerFX {
    private final Canvas canvas;

    public SignalVisualizerFX(int width, int height) {
        this.canvas = new Canvas(width, height);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void renderWaveform(List<Short> samples, double bpm) {
        // Defensive copy if needed to ensure immutability (optional)
        List<Short> safeSamples = (samples == null) ? List.of() : List.copyOf(samples);
        Platform.runLater(() -> draw(safeSamples, bpm));
    }

    private void draw(List<Short> samples, double bpm) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#121212"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (samples.isEmpty()) return;

        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();
        double gain = 2.5;
        double zoom = 1;

        gc.setStroke(Color.web("#82f6ee"));

        IntStream.range(1, samples.size())
                .forEach(i -> {
                    int x1 = scaleX(i - 1, samples.size(), width, zoom);
                    int x2 = scaleX(i, samples.size(), width, zoom);
                    int y1 = scaleY(samples.get(i - 1), height, gain);
                    int y2 = scaleY(samples.get(i), height, gain);
                    gc.strokeLine(x1, y1, x2, y2);
                });

        gc.setFill(Color.web("#ff7582"));
        gc.setFont(Font.font(20));
        gc.fillText("BPM: " + String.format("%.1f", bpm), 20, 40);
    }

    private int scaleX(int index, int totalSamples, int width, double zoom) {
        return (int) (index * width / (totalSamples * zoom));
    }

    private int scaleY(short sample, int height, double gain) {
        return height / 2 - (int) (sample * height / 2 * gain / Short.MAX_VALUE);
    }
}
