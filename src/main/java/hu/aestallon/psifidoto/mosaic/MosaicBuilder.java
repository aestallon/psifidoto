package hu.aestallon.psifidoto.mosaic;

import hu.aestallon.psifidoto.util.io.Directory;
import hu.aestallon.psifidoto.util.io.ImageFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MosaicBuilder {

    private ImageFile targetImage;

    private Set<ImageFile> imageFiles;
    private Tile.AspectRatio tileAspectRatio;
    private int tileCountHint;
    private int minRepetitionDistance;

    MosaicBuilder() {
        imageFiles = new TreeSet<>();
    }

    //--------------------------------------------------------------------------
    // Target- and tile image related building methods

    public MosaicBuilder withTilesFrom(Directory directory) {
        this.imageFiles.addAll(directory.imageFiles());
        return this;
    }

    public MosaicBuilder withTileFrom(ImageFile imageFile) {
        this.imageFiles.add(imageFile);
        return this;
    }

    public MosaicBuilder clearTiles() {
        this.imageFiles = new TreeSet<>();
        return this;
    }

    public MosaicBuilder targetImage(ImageFile imageFile) {
        this.targetImage = imageFile;
        return this;
    }

    //--------------------------------------------------------------------------
    // Mosaic layout related building methods

    public MosaicBuilder withTileCountOf(int tileCountHint) {
        this.tileCountHint = tileCountHint;
        return this;
    }

    public MosaicBuilder withTileAspectRatio(Tile.AspectRatio tileAspectRatio) {
        this.tileAspectRatio = tileAspectRatio;
        return this;
    }

    public MosaicBuilder withMinRepetitionDistance(int minRepetitionDistance) {
        this.minRepetitionDistance = minRepetitionDistance;
        return this;
    }

    //--------------------------------------------------------------------------
    // LIFTOFF

    public Mosaic build() {
        return new Mosaic(
                () -> loadImageFile(targetImage),
                () -> imageFiles.stream()
                        .map(this::loadImageFile)
                        .map(image -> new Tile(image, tileAspectRatio))
                        .collect(Collectors.toSet()),
                tileAspectRatio,
                tileCountHint,
                minRepetitionDistance
        );
    }

    //--------------------------------------------------------------------------
    // util

    private BufferedImage loadImageFile(ImageFile imageFile) {
        try (InputStream in = Files.newInputStream(imageFile.toPath())) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }







}
