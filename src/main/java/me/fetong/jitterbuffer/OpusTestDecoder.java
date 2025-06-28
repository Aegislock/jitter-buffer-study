package me.fetong.jitterbuffer;

import java.util.*;
import io.github.jaredmdobson.concentus.*;

public class OpusTestDecoder {
    private OpusDecoder decoder;

    public OpusTestDecoder() {
        this.decoder = new OpusDecoder(48000, 1);
    }

    public short[] decode(JitterPacket packet) {
        byte[] frame = packet.data;
        // Check formatting
    }

}