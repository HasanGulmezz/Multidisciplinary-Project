// File: OfflineSignalReader.java
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sound.sampled.*;

/**
 * Reads audio data from a WAV file offline using a functional approach.
 */
public final class OfflineSignalReader implements SignalReader {
    private final AudioInputStream audioStream;
    private final float sampleRate;

    public OfflineSignalReader(File file) throws Exception {
        this.audioStream = AudioSystem.getAudioInputStream(file);
        this.sampleRate = audioStream.getFormat().getSampleRate();
    }

    @Override
    public List<Short> readSignals() {
        try (audioStream) {
            AudioFormat format = audioStream.getFormat();
            byte[] buffer = new byte[format.getFrameSize() * 1024];
            int bytesRead = audioStream.read(buffer);

            return IntStream.range(0, bytesRead - 1)
                    .filter(i -> i % 2 == 0)
                    .mapToObj(i -> {
                        int low = buffer[i] & 0xFF;
                        int high = buffer[i + 1];
                        return (short) ((high << 8) | low);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public float getSampleRate() {
        return sampleRate;
    }
}
