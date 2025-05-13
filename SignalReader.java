// File: SignalReader.java
import java.util.List;

/**
 * Common interface for signal sources.
 */
public interface SignalReader {
    /**
     * Reads a batch of signal samples.
     * @return List of PCM samples as Short values.
     */
    List<Short> readSignals();
}

