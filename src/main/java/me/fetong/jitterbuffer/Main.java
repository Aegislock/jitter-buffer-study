package me.fetong.jitterbuffer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        File wavFile = new File("src/main/voiceTester.wav");

        // === Step 1: Read audio file ===
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
        AudioFormat format = audioStream.getFormat();

        // === Step 2: Get duration ===
        long frames = audioStream.getFrameLength();
        float frameRate = format.getFrameRate();
        float durationSeconds = frames / frameRate;
        int totalDurationMs = (int) (durationSeconds * 1000);

        System.out.println("Audio duration: " + totalDurationMs + " ms");

        int frameSize = 960;

        OpusTestEncoder encoder = new OpusTestEncoder(wavFile, frameSize);
        JitterBuffer buffer = new JitterBuffer(20); //

        SimulatedNetwork network = new SimulatedNetwork(
            20, 10f, 0.00f, 0.15f
        );

        OpusTestDecoder decoder = new OpusTestDecoder();
        AudioPlayer audioPlayer = new AudioPlayer();

        JitterTestSimulator simulator = new JitterTestSimulator(
            network,
            buffer,
            encoder,
            decoder,
            audioPlayer,
            20,    // base latency (ms)
            20,    // send interval (ms)
            20,    // tick step (ms)
            totalDurationMs,  // <- match length of WAV file
            false,
            false,
            true
        );

        simulator.run();
        audioPlayer.close();
    }
}