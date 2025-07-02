package me.fetong.jitterbuffer;

import javax.sound.sampled.*;

public class AudioPlayer {
    private SourceDataLine line;
    private final int sampleRate = 48000;
    private final int channels = 2;
    private final int bufferSize = 960 * 2; // 960 samples, 2 bytes per sample (16-bit)

    public AudioPlayer() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            16,
            channels,
            channels * 2,
            sampleRate,
            false // little endian
        );

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Line not supported: " + info);
        }

        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format, bufferSize * 4); // buffer for a few frames
        line.start();
    }

    // Accepts a short[] of PCM samples and plays them
    public void play(short[] samples) {
        byte[] bytes = new byte[samples.length * 2];

        for (int i = 0; i < samples.length; i++) {
            bytes[i * 2] = (byte) (samples[i] & 0xFF);           // little endian
            bytes[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }

        line.write(bytes, 0, bytes.length);
    }

    public void close() {
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
        }
    }
}