package hu.aestallon.psifidoto;

import hu.aestallon.psifidoto.mosaic.Mosaic;
import hu.aestallon.psifidoto.mosaic.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.stream.Stream;

public class Application {
    public static final String           TARGET_IMAGE            = "fosh";
    public static final String           RESULT_QUALIFIER        = "_5d_16to9_4000";
    public static final int              MIN_REPETITION_DISTANCE = 5;
    public static final Tile.AspectRatio ASPECT_RATIO            = Tile.AspectRatio.FULL_LANDSCAPE;
    public static final int              TILE_COUNT              = 4000;

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
        System.out.println(LocalTime.now() + " Application started.");
        Stream<BufferedImage> tileSet = Files.list(Path.of("src", "main", "resources", "testimages", "tiles", "downsized"))
                .map(Path::toString)
                .filter(s -> s.endsWith(".jpg"))
                .map(Application::openInputStream)
                .map(Application::readImage);
        System.out.println(LocalTime.now() + " Tile images loaded...");
        BufferedImage targetImage = ImageIO.read(openInputStream("src/main/resources/testimages/dream/fabric/" + TARGET_IMAGE + ".jpg"));
        System.out.println(LocalTime.now() + " Target image loading completed...");
        Mosaic m = new Mosaic(targetImage, tileSet, TILE_COUNT, MIN_REPETITION_DISTANCE, ASPECT_RATIO);
        System.out.println(LocalTime.now() + " Mosaic assessed...");
        BufferedImage result = m.export();
        System.out.println(LocalTime.now() + " Result export returned...");
        writeImage(result, "src/main/resources/testimages/dream/fabric/" + TARGET_IMAGE + RESULT_QUALIFIER + ".jpg");
        System.out.println(LocalTime.now() + " Write complete!");
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
