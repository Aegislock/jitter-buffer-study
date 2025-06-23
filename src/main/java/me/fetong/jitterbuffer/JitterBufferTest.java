package me.fetong.jitterbuffer;

public class JitterBufferTest {
    public static void main(String[] args) {
        testGetExactMatch();
        testGetInterpolation();
        testGetLatePacket();
        testGetFuturePacketFallback();
        testExpiredPacketSkipped();
        testLostPacketHandling();
        testInitialResetSync();
    }

    private static void testGetExactMatch() {
        System.out.println("Running testGetExactMatch...");
        JitterBuffer buffer = new JitterBuffer(20);
        buffer.setPointerTimestamp(0);
        buffer.setNextStop(0);

        JitterPacket p = new JitterPacket(new byte[]{1}, 0, 20, 0, 0, 0);
        buffer.put(p);

        JitterPacket out = new JitterPacket(null, 0, 0, 0, 0, 0);

        try {
            buffer.get(out);
            assertEquals(0, out.timestamp, "timestamp");
            assertEquals(20, out.span, "span");
            assertEquals(0, out.status, "status");
            System.out.println("PASS\n");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }

    private static void testGetInterpolation() {
        System.out.println("Running testGetInterpolation...");
        JitterBuffer buffer = new JitterBuffer(20);
        buffer.setPointerTimestamp(0);
        buffer.setinterpRequested(20);

        JitterPacket out = new JitterPacket(null, 0, 0, 0, 0, 0);
        try {
            buffer.get(out);
            assertEquals(2, out.status, "status");
            assertEquals(0, out.timestamp, "timestamp");
            assertEquals(20, out.span, "span");
            assertEquals(0, out.len, "len");
            System.out.println("PASS\n");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }

    private static void testGetLatePacket() {
        System.out.println("Running testGetLatePacket...");
        JitterBuffer buffer = new JitterBuffer(20);
        buffer.setPointerTimestamp(40);
        buffer.setNextStop(40);

        // Late but still spans the current pointer window
        JitterPacket p = new JitterPacket(new byte[]{2}, 30, 20, 1, 0, 0);
        buffer.put(p);

        JitterPacket out = new JitterPacket(null, 0, 0, 0, 0, 0);
        try {
            buffer.get(out);
            assertEquals(30, out.timestamp, "timestamp");
            assertEquals(20, out.span, "span");
            assertEquals(0, out.status, "status");
            System.out.println("PASS\n");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }

    private static void testGetFuturePacketFallback() {
        System.out.println("Running testGetFuturePacketFallback...");
        JitterBuffer buffer = new JitterBuffer(20);
        buffer.setPointerTimestamp(20);
        buffer.setNextStop(20);

        // No matching packet, but this one starts at 30
        JitterPacket p = new JitterPacket(new byte[]{3}, 30, 20, 2, 0, 0);
        buffer.put(p);

        JitterPacket out = new JitterPacket(null, 0, 0, 0, 0, 0);
        try {
            buffer.get(out);
            assertEquals(30, out.timestamp, "timestamp (future fallback)");
            assertEquals(20, out.span, "span");
            assertEquals(0, out.status, "status");
            System.out.println("PASS\n");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }

    private static void testExpiredPacketSkipped() {
        System.out.println("Running testExpiredPacketSkipped...");
        JitterBuffer buffer = new JitterBuffer(20);
        buffer.setResetState(false);
        buffer.setPointerTimestamp(100);
        buffer.setNextStop(100);

        // This packet should be skipped — it ended before the pointer
        JitterPacket expired = new JitterPacket(new byte[]{4}, 50, 20, 3, 0, 0);
        buffer.put(expired);

        JitterPacket out = new JitterPacket(null, 0, 0, 0, 0, 0);
        try {
            buffer.get(out);
            assertEquals(1, out.status, "status LOST");
            assertEquals(120, buffer.getPointerTimestamp(), "pointer advanced");
            System.out.println("PASS\n");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }

    private static void testLostPacketHandling() {
        System.out.println("Running testLostPacketHandling...");
        JitterBuffer buffer = new JitterBuffer(20);
        buffer.setPointerTimestamp(0);
        buffer.setNextStop(0);

        // No packets in buffer
        JitterPacket out = new JitterPacket(null, 0, 0, 0, 0, 0);
        try {
            buffer.get(out);
            assertEquals(1, out.status, "status LOST");
            assertEquals(20, buffer.getPointerTimestamp(), "pointer advanced");
            assertEquals(1, buffer.getLostCount(), "lost count incremented");
            System.out.println("PASS\n");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }

    private static void testInitialResetSync() {
        System.out.println("Running testInitialResetSync...");
        JitterBuffer buffer = new JitterBuffer(20);
        buffer.reset(); // force resetState = true

        // Add one packet — should cause sync during get()
        JitterPacket p = new JitterPacket(new byte[]{5}, 80, 20, 5, 0, 0);
        buffer.put(p);

        JitterPacket out = new JitterPacket(null, 0, 0, 0, 0, 0);
        try {
            buffer.get(out);
            assertEquals(80, out.timestamp, "reset sync works");
            assertEquals(false, buffer.isResetState(), "resetState cleared");
            System.out.println("PASS\n");
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }

    // === Assertion helpers ===
    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertEquals(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
