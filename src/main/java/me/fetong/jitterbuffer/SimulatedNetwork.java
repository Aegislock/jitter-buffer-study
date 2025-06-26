package me.fetong.jitterbuffer;

import java.util.*;

public class SimulatedNetwork {
    private int baseLatency;
    private float maxJitterMs;
    private float packetLossProbability;
    private float reorderProbability;

    private PriorityQueue<SimulatedPacket> packetQueue;

    public SimulatedNetwork(int baseLatency, float maxJitterMs,
                            float packetLossProbability, float reorderProbability) {
        this.baseLatency = baseLatency;
        this.maxJitterMs = maxJitterMs;
        this.packetLossProbability = packetLossProbability;
        this.reorderProbability = reorderProbability;
        this.packetQueue = new PriorityQueue<>(); // set comparator for the deliveryTime instance variable
    }

    // Add a SimulatedPacket to the PQ
    public void submitPacket(JitterPacket packet, long currentTimeMs) {
        // Roll for packet loss
        if (this.packetLossProbability > Math.random()) {
            return;
        }
        double jitter = Math.random() * this.maxJitterMs;
        double latency = baseLatency + jitter;
        // Casting here is only ok for simulation purposes where timing is in milliseconds
        SimulatedPacket simPacket = new SimulatedPacket(packet, (long) (currentTimeMs + latency));
        packetQueue.add(simPacket);
    }

    // Deliver the ready packets to the jitter buffer
    // Return all packets from packetQueue whose deliveryTime <= currentTimeMs
    public List<JitterPacket> deliverReadyPackets(long currentTimeMs, boolean reswapping) {
        List<JitterPacket> readyPackets = new ArrayList<>();
        while (!this.packetQueue.isEmpty() && 
                this.packetQueue.peek().deliveryTime <= currentTimeMs) {
            SimulatedPacket simPacket = this.packetQueue.poll();
            JitterPacket jitterPacket = simPacket.packet;
            readyPackets.add(jitterPacket);
        }
        JitterPacket previous = null;
        for (int i = 0; i < readyPackets.size(); i++) {
            boolean reordering = (this.reorderProbability > Math.random());
            if (reordering && previous != null) {
                JitterPacket temp = previous;
                readyPackets.set(i - 1, readyPackets.get(i));
                readyPackets.set(i, temp);
                if (!reswapping) {
                    i++;
                }
            }
            previous = readyPackets.get(i);
        }
        return readyPackets;
    }

    // Simulated Network Stuff
    // Implements a comparator
    private static class SimulatedPacket implements Comparable<SimulatedPacket> {
        public final JitterPacket packet;
        public final long deliveryTime;

        public SimulatedPacket(JitterPacket packet, long deliveryTime) {
            this.packet = packet;
            this.deliveryTime = deliveryTime;
        }

        // Comparator condition for the priority queue
        @Override
        public int compareTo(SimulatedPacket otherPacket) {
            return Long.compare(this.deliveryTime, otherPacket.deliveryTime);
        }
    }
}