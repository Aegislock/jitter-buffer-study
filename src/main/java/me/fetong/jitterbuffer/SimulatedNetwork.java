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
        this.packetQueue = new PriorityQueue<>();
    }

    // Add a SimulatedPacket to the PQ
    public void submitPacket(JitterPacket packet) {

    }

    // Deliver the ready packets to the jitter buffer
    // Return all packets from packetQueue whose deliveryTime <= currentTimeMs
    public List<JitterPacket> deliverReadyPackets(long currentTimeMs) {

    }

    // Simulated Network Stuff

    private static class SimulatedPacket implements Comparable<SimulatedPacket> {
        public final JitterPacket packet;
        public final long deliveryTime;

        public SimulatedPacket(JitterPacket packet, long deliveryTime) {
            this.packet = packet;
            this.deliveryTime = deliveryTime;
        }

        @Override
        public int compareTo(SimulatedPacket otherPacket) {
            return Long.compare(this.deliveryTime, otherPacket.deliveryTime);
        }
    }
}