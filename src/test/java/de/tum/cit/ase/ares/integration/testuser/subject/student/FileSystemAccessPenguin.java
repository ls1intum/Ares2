package de.tum.cit.ase.ares.integration.testuser.subject.student;

import de.tum.cit.ase.ares.integration.testuser.subject.thirdpartypackage.ThirdPartyPackagePenguin;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.logging.FileHandler;

/**
 * This class is used to access the file system using various classes from the {@link java.nio.file} package.
 * It is used to test if Ares can detect and handle file system access correctly.
 */
public class FileSystemAccessPenguin {

    /**
     * Access the file system using the {@link FileChannel} class.
     */
    void accessFileSystemViaFileChannel() {
        try {
            FileChannel fileChannel = FileChannel.open(null, null);
            fileChannel.read((ByteBuffer) null);
            fileChannel.write((ByteBuffer) null);
            fileChannel.force(true);
            fileChannel.position(0);
            fileChannel.size();
            fileChannel.tryLock();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link FileInputStream} class.
     */
    void accessFileSystemViaFileInputStream() {
        try {
            new FileInputStream("file.txt").read();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link FileOutputStream} class.
     */
    void accessFileSystemViaFileOutputStream() {
        // This method is intentionally left blank
        try {
            new FileOutputStream("file.txt").write(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link FileReader} class.
     */
    void accessFileSystemViaFileReader() {
        // This method is intentionally left blank
        try {
            FileReader reader = new FileReader("file.txt");
            reader.read();
            reader.read(new char[0]);
            reader.read(new char[0], 0, 0);
            reader.ready();
            reader.skip(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link FileWriter} class.
     */
    void accessFileSystemViaFileWriter() {
        // This method is intentionally left blank
        try {
            FileWriter writer = new FileWriter("file.txt");
            writer.write(0);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link RandomAccessFile} class.
     */
    void accessFileSystemViaRandomAccessFile() {
        // This method is intentionally left blank
        try {
            RandomAccessFile file = new RandomAccessFile("file.txt", "rw");
            file.read();
            file.read(new byte[0]);
            file.readBoolean();
            file.readByte();
            file.readChar();
            file.readDouble();
            file.readFloat();
            file.readInt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link BufferedReader} class.
     */
    void accessFileSystemViaFile() {
        // This method is intentionally left blank
        try {
            File file = new File("file.txt");
            file.createNewFile();
            file.delete();
            file.exists();
            file.isDirectory();
            file.isFile();
            file.isHidden();
            file.lastModified();
            file.length();
            file.list();
            file.listFiles();
            file.mkdir();
            file.mkdirs();
            file.renameTo(new File("file.txt"));
            file.setExecutable(true);
            file.setReadable(true);
            file.setWritable(true);
            file.toPath();
            file.toURI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link FileStore} class.
     */
    void accessFileSystemViaFileStore() {
        // This method is intentionally left blank
        try {
            FileStore store = Files.getFileStore(null);
            store.getTotalSpace();
            store.getUsableSpace();
            store.getUnallocatedSpace();
            store.isReadOnly();
            store.type();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link FileTime} class.
     */
    void accessFileSystemViaFileTime() {
        // This method is intentionally left blank
        try {
            Files.setLastModifiedTime(null, FileTime.fromMillis(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link Files} class.
     */
    void accessFileSystemViaFiles() {
        // This method is intentionally left blank
        try {
            Files.copy(null, null);
            Files.createDirectory(null);
            Files.createFile(null);
            Files.createLink(null, null);
            Files.createSymbolicLink(null, null);
            Files.createTempDirectory(null);
            Files.createTempFile(null, null);
            Files.createTempFile(null, null, null);
            Files.createDirectories(null);
            Files.createLink(null, null);
            Files.createSymbolicLink(null, null);
            Files.createTempDirectory(null);
            Files.createTempFile(null, null);
            Files.createTempFile(null, null, null);
            Files.delete(null);
            Files.deleteIfExists(null);
            Files.exists(null);
            Files.isReadable(null);
            Files.isWritable(null);
            Files.isExecutable(null);
            Files.isHidden(null);
            Files.getLastModifiedTime(null);
            Files.getAttribute(null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
            Files.getAttribute(null, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link FileSystemProvider} class.
     */
    void accessFileSystemViaFileSystemProvider() {
        try {
            FileSystemProvider provider = FileSystemProvider.installedProviders().get(0);
            provider.delete(Path.of("test.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.FileSystem} class.
     */
    void accessFileSystemViaFileSystem() {
        // This method is intentionally left blank
        try {
            FileSystem fileSystem = FileSystems.getDefault();
            fileSystem.close();
            fileSystem.isOpen();
            fileSystem.isReadOnly();
            fileSystem.provider();
            fileSystem.getRootDirectories();
            fileSystem.getFileStores();
            fileSystem.getPathMatcher(null);
            fileSystem.getUserPrincipalLookupService();
            fileSystem.newWatchService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.FileVisitor} class.
     */
    void accessFileSystemViaFileVisitor() {
        // This method is intentionally left blank
        try {
            SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            };
            visitor.postVisitDirectory(null, null);
            visitor.preVisitDirectory(null, null);
            visitor.visitFile(null, null);
            visitor.visitFileFailed(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.Path} class.
     */
    void accessFileSystemViaPath() {
        // This method is intentionally left blank
        try {
            Path path = Paths.get("file.txt");
            path.getFileName();
            path.getRoot();
            path.getParent();
            path.getNameCount();
            path.getName(0);
            path.subpath(0, 0);
            path.startsWith((Path) null);
            path.endsWith((Path) null);
            path.normalize();
            path.resolve((Path) null);
            path.relativize(null);
            path.toAbsolutePath();
            path.toRealPath();
            path.toUri();
            path.register(null, null);
            path.register(null, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.Paths} class.
     */
    void accessFileSystemViaPaths() {
        // This method is intentionally left blank
        try {
            Paths.get("file.txt");
            Paths.get("file.txt", "file.txt");
            Paths.get("file.txt", "file.txt", "file.txt");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.WatchService} class.
     */
    void accessFileSystemViaWatchService() {
        // This method is intentionally left blank
        try {
            WatchService service = FileSystems.getDefault().newWatchService();
            service.close();
            service.poll();
            service.poll(0, null);
            service.take();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.Watchable} class.
     */
    void accessFileSystemViaWatchable() {
        // This method is intentionally left blank
        try {
            Watchable watchable = Paths.get("file.txt");
            watchable.register(null, null);
            watchable.register(null, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.WatchEvent} class.
     */
    void accessFileSystemViaDesktop() {
        // This method is intentionally left blank
        try {
            Desktop.getDesktop().browse(null);
            Desktop.getDesktop().edit(null);
            Desktop.getDesktop().mail(null);
            Desktop.getDesktop().open(null);
            Desktop.getDesktop().print(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.WatchEvent} class.
     */
    void accessFileSystemViaFileHandler() {
        // This method is intentionally left blank
        try {
            new FileHandler("file.txt").close();
            new FileHandler("file.txt").flush();
            new FileHandler("file.txt").publish(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.WatchEvent} class.
     */
    @SuppressWarnings("deprecated")
    void accessFileSystemViaSoundBankReader() {
        try {
            // Get the default SoundbankReader
            MidiSystem.getSoundbank(new URL("soundbank.sf2"));
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Access the file system using the {@link java.nio.file.WatchEvent} class.
     */
    void accessFileSystemViaJFileChooser() {
        // Create a JFrame to hold the file chooser
        // Create a JButton to trigger the file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to open");

        // Show the file open dialog
        // Get the selected file
        File selectedFile = fileChooser.getSelectedFile();
        System.out.println("Selected file: " + selectedFile.getAbsolutePath());

        // Read and print the file contents
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Access the file system using a third party library which accesses the file system.
     */
    void checkAccessRecursive() throws IOException {
        ThirdPartyPackagePenguin.accessFileSystem();
    }

}
