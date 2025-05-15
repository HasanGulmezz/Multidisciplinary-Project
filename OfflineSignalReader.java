// File: OfflineSignalReader.java
import java.io.File;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import javax.sound.sampled.*;

/**
 * Reads audio data from a WAV file offline using a functional-friendly approach.
 */
public final class OfflineSignalReader implements SignalReader {
    private final AudioInputStream audioStream;
    private final float sampleRate;

    public OfflineSignalReader(File file) throws Exception {
        this.audioStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioStream.getFormat();

        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() != 16) {
            throw new UnsupportedAudioFileException("Only 16-bit PCM signed WAV files are supported.");
        }

        this.sampleRate = format.getSampleRate();
    }

    @Override
    public List<Short> readSignals() {
        try (audioStream) {
            AudioFormat format = audioStream.getFormat();
            int frameSize = format.getFrameSize();
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int bytesRead;
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            byte[] fullData = out.toByteArray();

            return IntStream.range(0, fullData.length / frameSize)
                    .mapToObj(i -> {
                        int index = i * frameSize;
                        int low = fullData[index] & 0xFF;
                        int high = fullData[index + 1];
                        return (short) ((high << 8) | low);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // return empty list on failure
        }
    }

    public float getSampleRate() {
        return sampleRate;
    }
}