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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Application {

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
        System.out.println(LocalTime.now() + " Application started.");
        Set<Tile> tileSet = Files.list(Path.of("src", "main", "resources", "testimages", "tiles", "downsized"))
                .map(Path::toString)
                .filter(s -> s.endsWith(".jpg"))
                .map(Application::openInputStream)
                .map(Application::readImage)
                .map(Tile::new)
                .flatMap(tile -> Stream.of(tile, new Tile(tile.getImage()), new Tile(tile.getImage())))
                .collect(Collectors.toSet());
        System.out.println(LocalTime.now() + " Tile images loaded...");
        BufferedImage targetImage = ImageIO.read(openInputStream("src/main/resources/testimages/dream/tomato.jpg"));
        System.out.println(LocalTime.now() + " Target image loading completed...");
        Mosaic m = new Mosaic(targetImage, tileSet);
        System.out.println(LocalTime.now() + " Mosaic assessed...");
        BufferedImage result = m.export();
        System.out.println(LocalTime.now() + " Result export returned...");
        writeImage(result, "src/main/resources/testimages/dream/tomato_MOSAIC_topDownBias_triplePic_refactor.jpg");
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
