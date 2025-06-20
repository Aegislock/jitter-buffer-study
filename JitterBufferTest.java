public class JitterBufferTest {
    public static void main(String[] args) {
        /*
        JitterBuffer buffer = new JitterBuffer(5); // delayStep = 5
        buffer.setNextStop(150);  

        // Case 1: On-time packet
        System.out.println("Test 1");
        JitterPacket p1 = new JitterPacket(new byte[]{1, 2}, 100, 20, 1);
        buffer.put(p1);
        buffer.debugPrint();

        // Case 2: Slightly late
        System.out.println("Test 2");
        buffer.setPointerTimestamp(115);
        JitterPacket p2 = new JitterPacket(new byte[]{3, 4}, 95, 10, 2);
        buffer.put(p2);  // expected to be dropped
        buffer.debugPrint();

        System.out.println("Test 3");
        // Case 3: Clearly late
        JitterPacket p3 = new JitterPacket(new byte[]{5, 6}, 80, 10, 3);
        buffer.put(p3);  // expected to be dropped
        buffer.debugPrint();

        System.out.println("Test 4");
        // Case 4: Fill buffer
        for (int i = 0; i < 10; i++) {
            JitterPacket p = new JitterPacket(new byte[]{(byte)i}, 120 + i * 10, 10, i + 10);
            buffer.put(p);
        }
        buffer.debugPrint();

        System.out.println("Test 5");
        // Case 5: Force eviction
        JitterPacket newest = new JitterPacket(new byte[]{99}, 300, 10, 999);
        buffer.put(newest);
        buffer.debugPrint();
        */
        testPutAndGetSinglePacket();
        testInterpolationTriggered();
        testGetWithEmptyBufferThrows();
        testGetSelectsBestFitPacket();
        testLostCountIncrementsIfNothingFound();
    }

    private static void testPutAndGetSinglePacket() {
        try {
            JitterBuffer buffer = new JitterBuffer(20);
            JitterPacket packet = new JitterPacket();
            packet.timestamp = 0;
            packet.span = 20;
            packet.data = new byte[] {1, 2, 3};
            packet.len = 3;
            packet.sequence = 1;

            buffer.put(packet);
            buffer.setNextStop(0);
            buffer.setPointerTimestamp(0);

            JitterPacket out = new JitterPacket();
            buffer.get(out);

            assertTrue(out.data != null && out.data[0] == 1, "Data matches");
            assertEquals(0, out.timestamp, "Timestamp matches");
            assertEquals(20, out.span, "Span matches");
            assertEquals(1, out.sequence, "Sequence matches");
            assertEquals(0, out.status, "Status OK");

            System.out.println("testPutAndGetSinglePacket PASSED");
        } catch (Exception e) {
            System.out.println("testPutAndGetSinglePacket FAILED: " + e.getMessage());
        }
    }

    private static void testInterpolationTriggered() {
        try {
            JitterBuffer buffer = new JitterBuffer(20);
            buffer.setinterpRequested(20);

            JitterPacket out = new JitterPacket();
            buffer.get(out);

            assertEquals(20, out.span, "Span");
            assertEquals(0, out.timestamp, "Timestamp");
            assertEquals(2, out.status, "Status: Interpolation");

            System.out.println("testInterpolationTriggered PASSED");
        } catch (Exception e) {
            System.out.println("testInterpolationTriggered FAILED: " + e.getMessage());
        }
    }

    private static void testGetWithEmptyBufferThrows() {
        try {
            JitterBuffer buffer = new JitterBuffer(20);
            buffer.reset(); // triggers resetState = true

            JitterPacket out = new JitterPacket();
            buffer.get(out); // Should throw

            System.out.println("testGetWithEmptyBufferThrows FAILED (did not throw)");
        } catch (Exception e) {
            System.out.println("testGetWithEmptyBufferThrows PASSED");
        }
    }

    private static void testGetSelectsBestFitPacket() {
        try {
            JitterBuffer buffer = new JitterBuffer(20);
            for (int i = 0; i < 3; i++) {
                JitterPacket p = new JitterPacket();
                p.timestamp = i * 20;
                p.span = 20;
                p.data = new byte[] {(byte)(i + 1)};
                p.len = 1;
                p.sequence = i;
                buffer.put(p);
            }

            buffer.debugPrint();

            buffer.setPointerTimestamp(0);
            buffer.setNextStop(0);

            JitterPacket out = new JitterPacket();
            buffer.get(out);

            assertEquals(0, out.timestamp, "Best-fit timestamp");
            assertEquals((byte)1, out.data[0], "Best-fit data");
            System.out.println("testGetSelectsBestFitPacket PASSED");
        } catch (Exception e) {
            System.out.println("testGetSelectsBestFitPacket FAILED: " + e.getMessage());
        }
    }

    private static void testLostCountIncrementsIfNothingFound() {
        try {
            JitterBuffer buffer = new JitterBuffer(20);
            buffer.setPointerTimestamp(100); // No packets in future

            JitterPacket out = new JitterPacket();
            buffer.get(out); // No packet should be found

            assertEquals(1, buffer.getLostCount(), "Lost count incremented");
            System.out.println("testLostCountIncrementsIfNothingFound PASSED");
        } catch (Exception e) {
            System.out.println("testLostCountIncrementsIfNothingFound FAILED: " + e.getMessage());
        }
    }

    // === Assertion Helpers ===
    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertEquals(byte expected, byte actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label + " failed");
        }
    }
}
