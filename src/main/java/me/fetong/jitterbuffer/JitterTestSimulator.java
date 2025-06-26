import me.fetong.jitterbuffer;

import java.util.*;
import io.github.jaredmdobson.concentus.*;

public class JitterTestSimulator {
    private SimulatedNetwork simulatedNetwork;
    private JitterBuffer jitterBuffer;
    private Encoder encoder;
    private Decoder decoder;
    private AudioPlayer audioPlayer;
    private int baseLatency;
    private int tickStepMs;
    private int totalDurationMs;
    private boolean reswapping;
    private boolean verbose;
    private boolean playbackEnabled;

    public JitterTestSimulator(SimulatedNetwork simulatedNetwork, JitterBuffer jitterBuffer, Encoder encoder, 
                                Decoder decoder, AudioPlayer audioPlayer, int baseLatency, int tickStepMs, 
                                int totalDurationMs, boolean reswapping, boolean verbose, boolean playbackEnabled) {
        if (simulatedNetwork == null || jitterBuffer == null || decoder == null) {
            throw new IllegalArgumentException("Core simulation components must not be null");
        }
        this.simulatedNetwork = simulatedNetwork;
        this.jitterBuffer = jitterBuffer;
        this.decoder = decoder;
        this.audioPlayer = audioPlayer;
        if (baseLatency <= 0) {
            throw new IllegalArgumentException("Latency must be a positive integer");
        }
        this.baseLatency = baseLatency;
        if (tickStepMs <= 0) {
            throw new IllegalArgumentException("Simulation step size must be a positive integer");
        }
        this.tickStepMs = tickStepMs;
        if (totalDurationMs <= 0) {
            throw new IllegalArgumentException("Total simulation duration must be a positive integer");
        }
        this.totalDurationMs = totalDurationMs;
        this.reswapping = reswapping;
        this.verbose = verbose;
        this.playbackEnabled = playbackEnabled;
    }

    public void run() {
        long currentTimeMs = 0;
        while (currentTimeMs < this.totalDurationMs) {
            // 1. Packet Generation

            // 2. Packet delivery

            // 3. Playback Attempt

            // 4. Advance Time
            currentTimeMs += this.tickStepMs;
        }
    }

    public void printSummary() {
        
    }
}