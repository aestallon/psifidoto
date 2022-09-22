package hu.aestallon.psifidoto.util.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Directory {

    private final Path dirPath;

    public static Directory of(Path path) {
        return new Directory(path);
    }

    private Directory(Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Provided path: " + path + " is not a directory!");
        }
        this.dirPath = Objects.requireNonNull(path);
    }

    public Path toPath() {
        return dirPath;
    }

    public Set<ImageFile> imageFiles() {
        try (var paths = Files.list(dirPath)) {
            return paths
                    .filter(ImageFile::isValidPath)
                    .map(ImageFile::of)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
}
