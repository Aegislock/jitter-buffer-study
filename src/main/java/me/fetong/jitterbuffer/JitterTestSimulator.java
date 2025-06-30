import me.fetong.jitterbuffer;

import java.util.*;
import java.io.File;
import java.io.IOException;
import io.github.jaredmdobson.concentus.*;

public class JitterTestSimulator {
     private SimulatedNetwork simulatedNetwork;
    private JitterBuffer jitterBuffer;
    private OpusTestEncoder encoder;
    private OpusTestDecoder decoder;
    private AudioPlayer audioPlayer;
    private int baseLatencyMs;
    private int sendIntervalMs;
    private int tickStepMs;
    private int totalDurationMs;
    private boolean reswapping;
    private boolean verbose;
    private boolean playbackEnabled;

    public JitterTestSimulator(SimulatedNetwork simulatedNetwork, JitterBuffer jitterBuffer, OpusTestEncoder encoder, 
                                OpusTestDecoder decoder, AudioPlayer audioPlayer, int baseLatencyMs, int sendIntervalMs, int tickStepMs, 
                                int totalDurationMs, boolean reswapping, boolean verbose, boolean playbackEnabled) throws IoException {
        if (simulatedNetwork == null || jitterBuffer == null || decoder == null) {
            throw new IllegalArgumentException("Core simulation components must not be null");
        }
        this.simulatedNetwork = simulatedNetwork;
        this.jitterBuffer = jitterBuffer;
        this.decoder = decoder;
        this.audioPlayer = audioPlayer;
        if (baseLatencyMs <= 0) {
            throw new IllegalArgumentException("Latency must be a positive integer");
        }
        this.baseLatency = baseLatency;
        if (sendIntervalMs <= 0) {
            throw new IllegalArgumentException("Packet send interval must be a positive integer");
        }
        this.sendIntervalMs = sendIntervalMs;
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
        // Initialize simulation time
        long currentTimeMs = 0;
        // Intialize packet index;
        int packetIndex = 0;
        // Initialize debug statistic metrics
        int played = 0;
        int sent = 0;
        int lost = 0;
        int interpolated = 0;
        int deliveredPackets = 0;
        // Encode the audio
        List<JitterPacket> packets = this.encoder.encode();
        while (currentTimeMs < this.totalDurationMs) {
            // 0. Check Correct Timeframe
            if (currentTimeMs % this.sendIntervalMs == 0) {
                // 1. Packet Generation
                if (this.encoder != null && packetIndex < packets.size()) {
                    JitterPacket current = packets.get(packetIndex);
                    this.simulatedNetwork.submitPacket(current, currentTimeMs);
                    sent++;
                    packetIndex++;
                }
                else {
                    // Synthetic Packet generation
                }
            }
            // 2. Packet delivery
            List<JitterPacket> delivered = this.simulatedNetwork.deliverReadyPackets(currentTimeMs, this.reswapping);
            for (int i = 0; i < delivered.size(); i++) {
                this.jitterBuffer.put(delivered.get(i));
                deliveredPackets++;
            }
            // 3. Playback Attempt
            JitterPacket outPacket = new JitterPacket();
            this.jitterBuffer.get(outPacket);
            if (outPacket.status != 1) {
                // Decoder stuff
            }
            else {
                // Decoder stuff
            }
            if (this.playbackEnabled) {
                // play audio
            }
            // 4. Advance Time
            currentTimeMs += this.tickStepMs;
        } 
    }

    public void printSummary() {

    }
}