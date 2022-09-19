package hu.aestallon.psifidoto.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {
    private ImageUtils() {}

    private static Color calculateAverageColour(BufferedImage image) {
        long r = 0, g = 0, b = 0;
        long pixelCount = (long) image.getHeight() * image.getWidth();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int pixel = image.getRGB(i, j);
                r += (pixel >> 16) & 0xFF;
                g += (pixel >> 8) & 0xFF;
                b += pixel & 0xFF;
            }
        }
        r = Math.round((double) r / pixelCount); g = Math.round((double) g / pixelCount); b = Math.round((double) b / pixelCount);
        return new Color((int) r, (int) g, (int) b);
    }

    public static Color calculateAverageColourSquared(BufferedImage image) {
        return calculateAverageColourOfRegion(image, 0, image.getWidth(), 0, image.getHeight());
    }

    public static Color calculateAverageColourOfRegion(BufferedImage image, int x0, int x1, int y0, int y1) {
        long r = 0, g = 0, b = 0;
        long pixelCount = (long) (x1 - x0) * (y1 - y0);
        for (int i = x0; i < x1; i++) {
            for (int j = y0; j < y1; j++) {
                int pixel = image.getRGB(i, j);

                int pixelR = (pixel >> 16) & 0xFF;
                r += pixelR * pixelR;

                int pixelG = (pixel >> 8) & 0xFF;
                g += pixelG * pixelG;

                int pixelB = pixel & 0xFF;
                b += pixelB * pixelB;
            }
        }
        r = r / pixelCount; g = g / pixelCount; b = b / pixelCount;
        r = Math.round(Math.sqrt(r)); g = Math.round(Math.sqrt(g)); b = Math.round(Math.sqrt(b));
        return new Color((int) r, (int) g, (int) b);
    }
}
