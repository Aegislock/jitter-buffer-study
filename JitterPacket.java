public class JitterPacket {
    public byte[] data;
    public int timestamp;
    public int span;
    public int sequence;
    public int len;

    public JitterPacket(byte[] data, int timestamp, int span, int sequence) {
        // constructor body
        this.data = data;
        this.timestamp = timestamp;
        this.span = span;
        this.sequence = sequence;
        this.len = this.data.length;
    }
}