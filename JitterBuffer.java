import Exception;

public class JitterBuffer {
    private static final int BUFFER_SIZE = 10;
    private JitterPacket[] packets;
    private int[] arrival;
    private int nextStop;
    private int pointerTimestamp;
    private int lastReturnedTimestamp;
    private int delayStep;
    private int lostCount;
    private int buffered;
    private int interpRequested;
    private boolean resetState;

    public JitterBuffer(int delayStep) {
        this.packets = new JitterPacket[BUFFER_SIZE];
        this.arrival = new int[BUFFER_SIZE];
        this.nextStop = 0; // Need to manually set
        this.pointerTimestamp = 0;
        this.lastReturnedTimestamp = 0;
        this.delayStep = delayStep;
        this.lostCount = 0;
        this.buffered = 0;
        this.interpRequested = 0;
        this.resetState = false;
    }

    // Insert a packet into the buffer
    public void put(JitterPacket packet) {
        boolean late = false;
        int i, j;
        // Cleanup (remove old packets that weren't played)
        if (!this.resetState) {
            for (i = 0; i < this.packets.length; i++) {
                // Do nothing if the packet is fully in the past
                // Don't need to free or destroy in Java
                if (packet.timestamp + packet.span + this.delayStep <= this.pointerTimestamp) {
                    continue;
                }
                else {
                    this.packets[i] = null;
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

    // Optional: pull the next usable packet
    public JitterPacket get(JitterPacket packet) throws Exception {
        // TODO: your implementation (optional)
        int i, j, opt;
        int start_offset = 0;
        // Sync on the first call
        if (this.resetState) {
            boolean found = false;
            int oldest_timestamp = 0;
            for (int i = 0; i < this.packets.length; i++) {
                if (this.packets[i].data != null && (!found || this.packets[i].timestamp < oldest_timestamp)) {
                    oldest_timestamp = this.packets[i].timestamp;
                    found = true;
                }
            }
            if (found) {
                this.resetState = 0;
                this.pointerTimestamp = oldest_timestamp;
                this.nextStop = oldest_timestamp;
            }
            else {
                packet.timestamp = 0;
                packet.span = this.interpRequested;
                packet.status = 1; // MISSING CODE
                throw new Exception("No packet found in the jitter buffer. Invalid jitter buffer.");
            }
        }
        this.pointerTimestamp = this.lastReturnedTimestamp;
        // Interpolation Logic 
        if (this.interpRequested != 0) {
            packet.timestamp = this.pointerTimestamp;
            packet.span = this.interpRequested;
            this.pointerTimestamp += this.interpRequested;
            packet.len = 0;
            this.interpRequested = 0;
            this.buffered = packet.span - this.delayStep;
            packet.status = 2;
            return;
        }
        
        // Search for best-fit packet
        // Option 1: Packet with the exact timestamp and spans the entire chunk
        for (i = 0; i < this.packets.length; i++) {
            if (this.packets[i] != null && this.packets[i].timestamp == this.pointerTimestamp
                && this.packets[i].timestamp + this.packets[i].span >= this.pointerTimestamp + this.delayStep) {
                break;
            }
        }
        // Option 2: Older packet that still spans the entire chunk 
        if (i == this.packets.length) {
            for (i = 0; i < this.packet.length; i++) {
                if (this.packets != null && this.packets[i].timestamp <= this.pointerTimestamp
                    && this.packets[i].timestamp + this.packets[i].span >= this.pointerTimestamp + this.delayStep) {
                    break;
                }
            }
        }
        // Option 3: Older packet that spans PART of the current chunk
        if (i == this.packets.length) {
            for (i = 0; i < this.packet.length; i++) {
                if (this.packets != null && this.packets[i].timestamp <= this.pointerTimestamp
                    && this.packets[i].timestamp + this.packets[i].span > this.pointerTimestamp) {
                    break;
                }
            }
        }
        // Option 4: Find earliest packet possible
        if (i == this.packets.length) {
            for ()
        }

        return null;
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
