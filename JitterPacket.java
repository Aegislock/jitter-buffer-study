public class JitterPacket {
    public byte[] data;
    public int timestamp;
    public int span;
    public int sequence;
    public int len;
    public int status;
    public int userData;

    public JitterPacket(byte[] data, int timestamp, int span, int sequence, int status, int userData) {
        // constructor body
        this.data = data;
        this.timestamp = timestamp;
        this.span = span;
        this.sequence = sequence;
        this.len = this.data.length;
        this.status = status;
        this.userData = userData;
    }
}