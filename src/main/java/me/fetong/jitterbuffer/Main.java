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

        System.out.println("Channels: " + format.getChannels());
        if (format.getChannels() == 1) {
            System.out.println("This WAV file is mono.");
        } else if (format.getChannels() == 2) {
            System.out.println("This WAV file is stereo.");
        } else {
            System.out.println("This WAV file has " + format.getChannels() + " channels.");
        }

        int frameSize = 960;

        OpusTestEncoder encoder = new OpusTestEncoder(wavFile, frameSize);
        JitterBuffer buffer = new JitterBuffer(20); //

        SimulatedNetwork network = new SimulatedNetwork(
            20, 10, 0.05f, 0.3f
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
            true,
            false,
            true
        );

        simulator.run();
        audioPlayer.close();
    }
}