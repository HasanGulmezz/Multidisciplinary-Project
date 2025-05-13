// File: OfflineSignalReader.java
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import javax.sound.sampled.*;

/**
 * Reads audio data from a WAV file offline.
 */
public class OfflineSignalReader implements SignalReader {
    private final AudioInputStream audioStream;
    private final float sampleRate;

    public OfflineSignalReader(File file) throws Exception {
        this.audioStream = AudioSystem.getAudioInputStream(file);
        this.sampleRate = audioStream.getFormat().getSampleRate();
    }

    @Override
    public List<Short> readSignals() {
        List<Short> samples = new ArrayList<>();
        try {
            AudioFormat format = audioStream.getFormat();
            byte[] buffer = new byte[format.getFrameSize() * 1024];
            int bytesRead;
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead - 1; i += 2) {
                    int low = buffer[i] & 0xff;
                    int high = buffer[i + 1];
                    short value = (short) ((high << 8) | low);
                    samples.add(value);
                }
            }
            audioStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return samples;
    }

    public float getSampleRate() {
        return sampleRate;
    }
}
