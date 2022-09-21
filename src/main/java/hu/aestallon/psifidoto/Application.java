package hu.aestallon.psifidoto;

import hu.aestallon.psifidoto.mosaic.Mosaic;
import hu.aestallon.psifidoto.mosaic.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Application {
    public static final Path TILE_DIR = Path.of(
            "src",
            "main",
            "resources",
            "testimages",
            "tiles",
            "downsized"
    );

    public static final int MIN_REPETITION_DISTANCE =     5;
    public static final int TILE_COUNT              = 4_000;

    public static void main(String[] args) {
        final String[] sourceImages = {"BGEDIT-4", "parrot", "branch"};

        for (String source : sourceImages) {
            for (var aspectRatio : Tile.AspectRatio.values()) {

                try {
                    run(source, MIN_REPETITION_DISTANCE, aspectRatio, TILE_COUNT);
                } catch (Exception anyException) {
                    System.err.println(
                            "Run: [[[" + source + " " + aspectRatio +
                            " min-rep: " + MIN_REPETITION_DISTANCE +
                            " tiles: " + TILE_COUNT + "]]] FAILED!"
                    );
                    anyException.printStackTrace();
                }
            }
        }

    }

    public static void run(String source,
                           int minRepetitionDistance,
                           Tile.AspectRatio tileAspectRatio,
                           int tileCount) throws Exception {
        final String srcPath         = "src/main/resources/testimages/dream/fabric/" +
                                       source + ".jpg";
        final String resultQualifier = "_" + minRepetitionDistance +
                                       "d_" + tileAspectRatio +
                                       "_" + tileCount;
        final String destPath        = "src/main/resources/testimages/dream/fabric/" +
                                       source + resultQualifier + ".jpg";

        System.out.println("Run: [[[" + source+resultQualifier + "]]] started!");
        try (Stream<Path> paths = Files.list(TILE_DIR)) {
            Stream<BufferedImage> tileSet = paths
                    .map(Path::toString)
                    .filter(s -> s.endsWith(".jpg"))
                    .map(Application::openInputStream)
                    .map(Application::readImage);
            BufferedImage targetImage = ImageIO.read(openInputStream(srcPath));
            Mosaic m = new Mosaic(
                    targetImage,
                    tileSet,
                    tileCount,
                    minRepetitionDistance,
                    tileAspectRatio
            );
            BufferedImage result = m.export();
            writeImage(result, destPath);
            System.out.println("Run: [[[" + source+resultQualifier + "]]] finished!");
        }
    }

    private static InputStream openInputStream(String path) {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage readImage(InputStream in) {
        try {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void writeImage(BufferedImage image, String path) {
        try {
            OutputStream out = new FileOutputStream(path);
            ImageIO.write(image, "jpg", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
