// File: FeatureExtractor.java
import java.util.ArrayList;
import java.util.List;

/**
 * Provides pure functions for signal feature extraction using sample indices.
 */
public class FeatureExtractor {
    /**
     * Detects peak times (in seconds) from raw samples.
     * @param samples List of PCM samples
     * @param threshold Amplitude threshold
     * @param cooldownSamples Cooldown in number of samples
     * @param sampleRate Sampling rate in Hz
     * @return List of peak timestamps in seconds
     */
    public static List<Double> detectPeaks(List<Short> samples, int threshold, int cooldownSamples, double sampleRate) {
        List<Double> peakTimes = new ArrayList<>();
        int cooldownCounter = 0;
        for (int i = 0; i < samples.size(); i++) {
            short s = samples.get(i);
            if (cooldownCounter > 0) {
                cooldownCounter--;
            } else if (Math.abs(s) > threshold) {
                peakTimes.add(i / sampleRate);
                cooldownCounter = cooldownSamples;
            }
        }
        return peakTimes;
    }

    /**
     * Calculates BPM from peak timestamps in seconds.
     * @param peakTimes List of peak timestamps
     * @return Beats per minute
     */
    public static double calculateBPM(List<Double> peakTimes) {
        if (peakTimes.size() < 2) return 0;
        double sumDiff = 0.0;
        for (int i = 1; i < peakTimes.size(); i++) {
            sumDiff += (peakTimes.get(i) - peakTimes.get(i - 1));
        }
        double avgInterval = sumDiff / (peakTimes.size() - 1);
        return 60.0 / avgInterval;
    }
}
