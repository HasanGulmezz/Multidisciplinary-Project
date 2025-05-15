import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides pure functions for signal feature extraction using sample indices.
 */
public final class FeatureExtractor {
    private FeatureExtractor() {}

    /**
     * Detects peak times (in seconds) from raw samples.
     * @param samples List of PCM samples
     * @param threshold Amplitude threshold
     * @param cooldownSamples Cooldown in number of samples
     * @param sampleRate Sampling rate in Hz
     * @return List of peak timestamps in seconds
     */
    public static List<Double> detectPeaks(List<Short> samples, int threshold, int cooldownSamples, double sampleRate) {
        final int size = samples.size();
        int[] cooldown = {0};
        return IntStream.range(0, size)
                .filter(i -> {
                    short value = samples.get(i);
                    boolean isPeak = cooldown[0] <= 0 && Math.abs(value) > threshold;
                    if (isPeak) cooldown[0] = cooldownSamples;
                    else if (cooldown[0] > 0) cooldown[0]--;
                    return isPeak;
                })
                .mapToObj(i -> i / sampleRate)
                .collect(Collectors.toList());
    }

    /**
     * Calculates BPM from peak timestamps in seconds.
     * @param peakTimes List of peak timestamps
     * @return Beats per minute
     */
    public static double calculateBPM(List<Double> peakTimes) {
        if (peakTimes.size() < 2) return 0;
        return 60.0 * (peakTimes.size() - 1) /
                IntStream.range(1, peakTimes.size())
                        .mapToDouble(i -> peakTimes.get(i) - peakTimes.get(i - 1))
                        .sum();
    }
}