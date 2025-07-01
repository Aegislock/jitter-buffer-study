package me.fetong.jitterbuffer;

import java.util.*;
import io.github.jaredmdobson.concentus.*;

public class OpusTestDecoder {
    private OpusDecoder decoder;

    public OpusTestDecoder() throws OpusException {
        this.decoder = new OpusDecoder(48000, 1);
    }

    public short[] decode(JitterPacket packet) throws OpusException {
        short[] decoded = new short[packet.span]; // or use fixed size like 960
        if (packet.status == 0) {
            // Normal decode
            decoder.decode(packet.data, 0, packet.data.length, decoded, 0, packet.span, false);
        } else if (packet.status == 1) {
            // Packet loss concealment (PLC)
            decoder.decode(new byte[0], 0, 0, decoded, 0, packet.span, false);
        } else {
            // Interpolated: return silence
            return new short[packet.span]; // silence
        }
        return decoded;
    }
}