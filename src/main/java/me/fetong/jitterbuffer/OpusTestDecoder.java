package me.fetong.jitterbuffer;

import java.util.*;
import io.github.jaredmdobson.concentus.*;

public class OpusTestDecoder {
    private OpusDecoder decoder;

    public OpusTestDecoder() throws OpusException {
        this.decoder = new OpusDecoder(48000, 1);
    }

    public short[] decode(JitterPacket packet) throws OpusException {
        short[] decoded = new short[960]; // or use fixed size like 960

        // Print debug info
        System.out.println("→ Decoding packet: sequence = " + packet.sequence +
                       ", status=" + packet.status +
                       ", len=" + packet.len +
                       ", span=" + packet.span +
                       ", data.length=" + (packet.data != null ? packet.data.length : -1));

        if (packet.status == 0) {
            // Normal decode
            System.out.println("Packet type NORMAL | index: " + packet.sequence + " | len: " + packet.len + " | data.length: " + packet.data.length + " | span: " + packet.span);
            decoder.decode(packet.data, 0, packet.data.length, decoded, 0, 960, false);
        } else if (packet.status == 1) {
            // Packet loss concealment (PLC)
            decoder.decode(null, 0, 0, decoded, 0, packet.span, false);
        } else {
            // Interpolated: return silence
            return new short[packet.span]; // silence
        }
        return decoded;
    }
}