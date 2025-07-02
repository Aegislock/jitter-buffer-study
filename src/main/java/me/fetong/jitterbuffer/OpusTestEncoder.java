package me.fetong.jitterbuffer;

import java.util.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import io.github.jaredmdobson.concentus.*;

public class OpusTestEncoder {
    private File file;
    private int frameSize;
    private final OpusEncoder encoder;

    public OpusTestEncoder(File file, int frameSize) throws OpusException {
        this.file = file;
        this.frameSize = frameSize;
        this.encoder = new OpusEncoder(48000, 1, OpusApplication.OPUS_APPLICATION_VOIP);
    }

    public List<JitterPacket> encode() throws IOException, UnsupportedAudioFileException, OpusException {
        short[] pcmShorts = this.extractPCM();
        List<short[]> splits = this.splitIntoFrames(pcmShorts, this.frameSize);
        List<JitterPacket> bufferInput = this.encodeAndPacketize(splits);
        return bufferInput;
    }

    private List<JitterPacket> encodeAndPacketize(List<short[]> frames) throws IOException, UnsupportedAudioFileException, OpusException {
        List<JitterPacket> bufferInput = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            // 1276 because of Opus frame size limit of 1275 + 1 frame for a table of contents / padding  
            byte[] encodedBuffer = new byte[1276];
            // 960 samples / frame because Opus has a few valid frame sizes for 48kHz mono channel. 
            // 400 samples is invalid beacuse it does not fit a nice frame duration and Opus will not
            // try to encode partial frames. 
            // TLDR: Opus enforces strict frame lengths
            int len = this.encoder.encode(frames.get(i), 0, 960, encodedBuffer, 0, encodedBuffer.length);
            // There may be empty space at the end of encodedBuffer that is not filled
            // We use copyOf here to only copy useful information into encodedFrame
            byte[] encodedFrame = Arrays.copyOf(encodedBuffer, len);
            JitterPacket packet = new JitterPacket(encodedFrame, i * frameSize, i, 0, 0);
            bufferInput.add(packet);
        }
        return bufferInput;
    }

    // Returns flattened array
    private short[] extractPCM() throws UnsupportedAudioFileException, IOException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.file);
        // Validate the correct format (16-bit PCM)
        AudioFormat format = audioStream.getFormat();
        // Check signed-ness and if the audio sample size is 16-bits
        // Decoding logic aassumes each sample is exactly 2 bytes and maps to a signed short
        // If format were different, the conversion would interpet the bytes incorrectly
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() != 16) {
            throw new UnsupportedAudioFileException("Only 16-bit signed PCM supported");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = audioStream.read(buffer)) != -1) {
            // Keep writing until we can't write anymore, internal variable in baos keeping track of 
            // how much data we have written so that we don't have to rewrite from the beginning 
            baos.write(buffer, 0, bytesRead); 
        }
        byte[] pcmBytes = baos.toByteArray();
        // Wraps byte[] array so we can read any type of data from it (short, int, long, etc.)
        ByteBuffer bb = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN); // little endian beacuse WAV is decoded from LSB to MSB
        short[] pcmShorts = new short[pcmBytes.length / 2];

        for (int i = 0; i < pcmShorts.length; i++) {
            pcmShorts[i] = bb.getShort();
        }
        return pcmShorts;
    }

    private List<short[]> splitIntoFrames(short[] pcm, int frameSize) {
        int i;
        List<short[]> splits = new ArrayList<>();
        for (i = 0; i < pcm.length - frameSize; i += frameSize) {
            short[] split = Arrays.copyOfRange(pcm, i, i + frameSize);
            splits.add(split);
        }
        if (i < pcm.length) {
            short[] padded = new short[frameSize]; // always 960
            for (int j = 0; j < pcm.length - i; j++) {
                padded[j] = pcm[i + j]; // copy remainder
            }
            // rest remains zero (silence)
            splits.add(padded);
        }
        return splits;
    }

}
