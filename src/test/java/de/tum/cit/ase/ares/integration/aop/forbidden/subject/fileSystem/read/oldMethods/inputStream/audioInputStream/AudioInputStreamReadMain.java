package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.inputStream.audioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.FileInputStream;
import java.io.IOException;

public class AudioInputStreamReadMain {

    private AudioInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    private static AudioInputStream getAudioInputStream(FileInputStream fis) {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        return new AudioInputStream(fis, format, AudioSystem.NOT_SPECIFIED);
    }

    public static int accessFileSystemViaAudioInputStreamRead() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             AudioInputStream reader = getAudioInputStream(fis)) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaAudioInputStreamReadByteArray() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             AudioInputStream reader = getAudioInputStream(fis)) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaAudioInputStreamReadByteArrayOffsetLength() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             AudioInputStream reader = getAudioInputStream(fis)) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaAudioInputStreamReadAllBytes() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             AudioInputStream reader = getAudioInputStream(fis)) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaAudioInputStreamReadNBytes() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             AudioInputStream reader = getAudioInputStream(fis)) {
            return reader.readNBytes(1024);
        }
    }
}
