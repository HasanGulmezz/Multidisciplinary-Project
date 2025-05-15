import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

/**
 * Orchestrates reading, accumulating, processing, and full-waveform visualization.
 */
public final class SignalProcessor {
    private final SignalReader reader;
    private final SignalVisualizerFX visualizer;
    private final int threshold;
    private final int cooldownSamples;
    private final double sampleRate;

    private final List<Short> accumulatedSamples = new CopyOnWriteArrayList<>();
    private Thread processingThread;

    public SignalProcessor(SignalReader reader, SignalVisualizerFX visualizer,
                           int threshold, int cooldownSamples, double sampleRate) {
        this.reader = reader;
        this.visualizer = visualizer;
        this.threshold = threshold;
        this.cooldownSamples = cooldownSamples;
        this.sampleRate = sampleRate;
    }

    public void startProcessingRealtime() {
        processingThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    List<Short> batch = reader.readSignals();
                    if (batch != null && !batch.isEmpty()) {
                        accumulatedSamples.addAll(batch);
                        processAndRender(List.copyOf(accumulatedSamples));
                    }
                }
            } catch (Exception ignored) {
                // Thread interrupted or other exception; exit cleanly
            }
        });
        processingThread.start();
    }

    public void startProcessingOffline() {
        List<Short> all = reader.readSignals();
        accumulatedSamples.clear();
        if (all != null && !all.isEmpty()) {
            accumulatedSamples.addAll(all);
            processAndRender(List.copyOf(accumulatedSamples));
        }
    }

    public void stop() {
        if (processingThread != null) {
            processingThread.interrupt();
            processingThread = null;
        }
    }

    private void processAndRender(List<Short> samples) {
        List<Double> peaks = FeatureExtractor.detectPeaks(samples, threshold, cooldownSamples, sampleRate);
        double bpm = FeatureExtractor.calculateBPM(peaks);
        SwingUtilities.invokeLater(() -> visualizer.renderWaveform(samples, bpm));
    }
}
