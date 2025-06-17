public class JitterBufferTest {
    public static void main(String[] args) {
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
    }

    private static void assertEquals(Object a, Object b, String message) {
        if (!a.equals(b)) throw new AssertionError("FAIL: " + message + " | Expected: " + b + ", Got: " + a);
    }
}
