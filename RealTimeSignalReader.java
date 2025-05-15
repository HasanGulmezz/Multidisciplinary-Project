// File: RealTimeSignalReader.java
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sound.sampled.*;

/**
 * Reads audio data in real-time from the sound card using a functional approach.
 */
public final class RealTimeSignalReader implements SignalReader {
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
        return IntStream.range(0, bytesRead - 1)
                .filter(i -> i % 2 == 0)
                .mapToObj(i -> {
                    int low = byteBuffer[i] & 0xff;
                    int high = byteBuffer[i + 1];
                    return (short) ((high << 8) | low);
                })
                .collect(Collectors.toList());
    }

    public void close() {
        line.stop();
        line.close();
    }

    public float getSampleRate() {
        return sampleRate;
    }
}