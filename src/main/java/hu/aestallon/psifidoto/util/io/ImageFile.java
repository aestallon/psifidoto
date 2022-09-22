package hu.aestallon.psifidoto.util.io;

import java.nio.file.Path;
import java.util.Arrays;

public class ImageFile implements Comparable<ImageFile> {

    public enum SupportedFormat {
        BMP, TIFF, PNG, JPG, JPEG
    }

    public static boolean isValidPath(Path path) {
        String fileName = path.getFileName().toString();
        return Arrays.stream(SupportedFormat.values())
                .map(format -> "." + format.toString())
                .anyMatch(ending -> fileName.toUpperCase().endsWith(ending));
    }

    public static ImageFile of(Path path) {
        return new ImageFile(path);
    }

    private final Path filePath;

    private ImageFile(Path path) {
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Provided path does not point to a supported image file format!");
        }
        this.filePath = path;
    }

    public Path toPath() {
        return filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageFile imageFile = (ImageFile) o;
        return this.filePath.equals(imageFile.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    @Override
    public int compareTo(ImageFile another) {
        return this.filePath.compareTo(another.filePath);
    }
}
