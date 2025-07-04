package me.fetong.jitterbuffer;

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
    private boolean playBuffered;
    private short[] lastValidPCM;

    public JitterTestSimulator(SimulatedNetwork simulatedNetwork, JitterBuffer jitterBuffer, OpusTestEncoder encoder, 
                                OpusTestDecoder decoder, AudioPlayer audioPlayer, int baseLatencyMs, int sendIntervalMs, int tickStepMs, 
                                int totalDurationMs, boolean reswapping, boolean verbose, boolean playBuffered) throws IOException {
        if (simulatedNetwork == null || jitterBuffer == null || decoder == null) {
            throw new IllegalArgumentException("Core simulation components must not be null");
        }
        this.simulatedNetwork = simulatedNetwork;
        this.jitterBuffer = jitterBuffer;
        this.encoder = encoder;
        this.decoder = decoder;
        this.audioPlayer = audioPlayer;
        if (baseLatencyMs <= 0) {
            throw new IllegalArgumentException("Latency must be a positive integer");
        }
        this.baseLatencyMs = baseLatencyMs;
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
        this.playBuffered = playBuffered;
        this.lastValidPCM = null;
    }

    public void run() throws Exception {
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
            // 1. Deliver packets from *previous* ticks first
            List<JitterPacket> delivered = this.simulatedNetwork.deliverReadyPackets(currentTimeMs, this.reswapping);
            // 2. Then send the current packet for future delivery
            if (currentTimeMs % this.sendIntervalMs == 0) {
                if (this.encoder != null && packetIndex < packets.size()) {
                    JitterPacket current = packets.get(packetIndex);
                    this.simulatedNetwork.submitPacket(current, currentTimeMs);
                    sent++;
                    packetIndex++;
                }
            }
            if (!playBuffered) {
                for (JitterPacket pkt : delivered) {
                    short[] decoded = decoder.decode(pkt);
                    this.audioPlayer.play(decoded);  // raw playback
                }
            }
            else {
                for (int i = 0; i < delivered.size(); i++) {
                    this.jitterBuffer.put(delivered.get(i));
                    deliveredPackets++;
                }
                // 3. Playback Attempt
                JitterPacket outPacket = new JitterPacket();
                if (currentTimeMs >= 40) {
                    this.jitterBuffer.get(outPacket);
                    short[] decoded;
                    if (outPacket.status == 2) {
                        if (this.lastValidPCM != null) {
                            decoded = applyAttenuation();
                        }
                        else {
                            decoded = new short[960];
                        }
                        interpolated++;
                    } 
                    else {
                        decoded = decoder.decode(outPacket);
                        if (outPacket.status == 1) {
                            lost++;
                        }
                    }
                    this.lastValidPCM = decoded;
                    this.audioPlayer.play(decoded);
                    played++;
                }
            }
            // 4. Advance Time
            currentTimeMs += this.tickStepMs;
        } 
    }

    private short[] applyAttenuation() {
        short[] input = this.lastValidPCM;
        short[] output = new short[input.length];
        float attenuationFactor = (float) Math.pow(0.8, this.jitterBuffer.getLostCount());
        for (int i = 0; i < input.length; i++) {
            int sample = (int) (input[i] * attenuationFactor);
            sample = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample));
            output[i] = (short) sample;
        }
        return output;
    }

    public void printSummary() {

    }
}