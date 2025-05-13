
// File: SignalProcessor.java
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
/**
 * Orchestrates reading, accumulating, processing, and full-waveform visualization.
 */
public class SignalProcessor {
    private final SignalReader reader;
    private final SignalVisualizerFX visualizer;
    private final int threshold;
    private final int cooldownSamples;
    private final double sampleRate;
    private Thread processingThread;
    private final List<Short> accumulatedSamples = new ArrayList<>();

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
            while (!Thread.currentThread().isInterrupted()) {
                List<Short> batch = reader.readSignals();
                if (batch != null && !batch.isEmpty()) {
                    accumulatedSamples.addAll(batch);
                    processAndRender(accumulatedSamples);
                }
            }
        });
        processingThread.start();
    }

    public void startProcessingOffline() {
        List<Short> all = reader.readSignals();
        accumulatedSamples.clear();
        if (all != null) {
            accumulatedSamples.addAll(all);
            processAndRender(accumulatedSamples);
        }
    }

    public void stop() {
        if (processingThread != null) {
            processingThread.interrupt();
        }
    }

    private void processAndRender(List<Short> samples) {
        List<Double> peaks = FeatureExtractor.detectPeaks(samples, threshold, cooldownSamples, sampleRate);
        double bpm = FeatureExtractor.calculateBPM(peaks);
        SwingUtilities.invokeLater(() -> visualizer.renderWaveform(samples, bpm));
    }
}
