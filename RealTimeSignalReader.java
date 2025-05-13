// File: RealTimeSignalReader.java
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;

/**
 * Reads audio data in real-time from the sound card.
 */
public class RealTimeSignalReader implements SignalReader {
    private final TargetDataLine line;
    private final int bufferSize;
    private final float sampleRate;

    public RealTimeSignalReader(AudioFormat format, int bufferSize) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        this.line = (TargetDataLine) AudioSystem.getLine(info);
        this.line.open(format);
        this.line.start();
        this.bufferSize = bufferSize;
        this.sampleRate = format.getSampleRate();
    }

    @Override
    public List<Short> readSignals() {
        byte[] byteBuffer = new byte[bufferSize];
        int bytesRead = line.read(byteBuffer, 0, bufferSize);
        List<Short> samples = new ArrayList<>(bytesRead / 2);
        for (int i = 0; i < bytesRead - 1; i += 2) {
            int low = byteBuffer[i] & 0xff;
            int high = byteBuffer[i + 1];
            short value = (short) ((high << 8) | low);
            samples.add(value);
        }
        return samples;
    }

    public void close() {
        line.stop();
        line.close();
    }

    public float getSampleRate() {
        return sampleRate;
    }
}
