public class JitterBuffer {
    private static final int BUFFER_SIZE = 10;
    private JitterPacket[] packets;
    private int[] arrival;
    private int nextStop;
    private int pointerTimestamp;
    private int delayStep;
    private int lostCount;
    private boolean resetState;

    public JitterBuffer(int delayStep) {
        this.packets = new JitterPacket[BUFFER_SIZE];
        this.arrival = new int[BUFFER_SIZE];
        this.nextStop = 0;
        this.pointerTimestamp = 0;
        this.delayStep = delayStep;
        this.lostCount = 0;
        this.resetState = false;
    }

    // Insert a packet into the buffer
    public void put(JitterPacket packet) {
        boolean late = false;
        int i, j;
        // Cleanup (remove old packets that weren't played)
        if (!this.resetState) {
            for (i = 0; i < packet.len; i++) {
                // Do nothing if the packet is fully in the past
                // Don't need to free or destroy in Java
                if (packet.timestamp + packet.span + this.delayStep < this.pointerTimestamp) {
                    continue;
                }
            }
        }
        // If packet is late but its span is yet to be reached (useful but late)
        // Control condition is ok because we have already removed useless packets
        if (!this.resetState && packet.timestamp < this.nextStop) {
            // update timings (adaptive filter, don't have to worry about yet)
            late = true;
        }
        // Resync buffer
        if (lostCount > 20) {
            this.reset();
        }
        if (this.resetState || packet.timestamp + packet.span + this.delayStep >= this.pointerTimestamp) {
            for (i = 0; i < this.packets.length; i++) {
                if (this.packets[i] == null || this.packets[i].data == null) {
                    break;
                }
            }
            if (i == this.packets.length) {
                i = 0;
                int earliest = this.packets[0].timestamp;
                for (j = 1; j < this.packets.length; j++) {
                    if (this.packets[j].timestamp < earliest) {
                        earliest = this.packets[j].timestamp;
                        i = j;
                    }
                }
            }
            this.packets[i] = packet;
            if (this.resetState || late) {
                arrival[i] = 0;
            }
            else {
                arrival[i] = this.nextStop; 
            }
        }
    }

    public JitterPacket[] getPackets() {
        return packets;
    }

    public int[] getArrival() {
        return arrival;
    }

    public void setNextStop(int nextStop) {
        this.nextStop = nextStop;
    }

    public int getNextStop() {
        return nextStop;
    }

    public void setPointerTimestamp(int pointerTimestamp) {
        this.pointerTimestamp = pointerTimestamp;
    }

    public int getPointerTimestamp() {
        return pointerTimestamp;
    }

    public int getDelayStep() {
        return delayStep;
    }

    public boolean isResetState() {
        return resetState;
    }

    public int getLostCount() {
        return lostCount;
    }

    // Optional: pull the next usable packet
    public JitterPacket get() {
        // TODO: your implementation (optional)
        return null;
    }

    // Optional: manually reset the buffer
    public void reset() {
        // TODO
    }

    // Debug function to print buffer state
    public void debugPrint() {
        System.out.println("=== Jitter Buffer State ===");
        for (int i = 0; i < packets.length; i++) {
            JitterPacket p = packets[i];
            if (p != null) {
                System.out.printf("Slot %2d | ts: %d | span: %d | seq: %d | len: %d | arrival: %d\n",
                    i, p.timestamp, p.span, p.sequence, p.len, arrival[i]);
            } else {
                System.out.printf("Slot %2d | (empty)\n", i);
            }
        }
        System.out.println("pointerTimestamp = " + pointerTimestamp);
        System.out.println("nextStop         = " + nextStop);
        System.out.println("resetState       = " + resetState);
        System.out.println("===========================\n");
    }
}
