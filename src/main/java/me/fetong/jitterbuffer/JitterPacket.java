package me.fetong.jitterbuffer;

public class JitterPacket {
    public byte[] data;
    public int timestamp;
    public int span;
    public int sequence;
    public int len;
    public int status;
    public int userData;

    public JitterPacket() {
        this.data = new byte[0];
        this.timestamp = 0;
        this.span = 960;
        this.sequence = 0;
        this.status = 0;
        this.userData = 0;
        this.len = 0;
    }

    public JitterPacket(byte[] data, int timestamp, int sequence, int status, int userData) {
        this.data = (data != null) ? data : new byte[0]; // default to empty array
        this.timestamp = timestamp;
        this.span = 960;
        this.sequence = sequence;
        this.len = this.data.length;
        this.status = status;
        this.userData = userData;
    }
}