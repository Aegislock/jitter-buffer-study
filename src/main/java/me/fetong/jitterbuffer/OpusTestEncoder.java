import java.utils.*;
import javax.sound.sampled.*;
import java.io.File;
import io.github.jaredmdobson.concentus.*;

public class OpusTestDecoder {
    private File file;
    private byte[] opusEncoded;

    public OpusTestDecoder(File file) {
        this.file = file;
    }

    public void encode() {
        OpusEncoder encoder = new OpusEncoder(48000, 1, OpusApplication.VOIP);
        short[] pcmShorts = this.extracPCM()

        byte[] encoded = new byte[1276]; // Opus frame max lenghth + 1 byte for the header
        int len = encoder.encode()
    }

    // Returns flattened array
    private short[] extractPCM() throws UnsupportedAudioFileException, IOException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.file);
        // Validate the correct format (16-bit PCM)
        AudioFormat format = audioStream.getFormat();
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
        ByteBuffer bb = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN);
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
        if (i != pcm.length) {
            short[] lastSplit = new short[pcm.length - i];
            int index = 0;
            for (int j = i; j < pcm.length; j++) {
                lastSplit[index] = pcm[j];
                index++;
            }
            splits.add(lastSplit);
        }
        return splits;
    }

}
