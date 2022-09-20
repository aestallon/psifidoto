package hu.aestallon.psifidoto.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    private static int idCounter = 0;

    public enum AspectRatio {
        SQUARE(1, 1),
        FULL_LANDSCAPE(4, 3),
        FULL_PORTRAIT(3, 4),
        WIDE_LANDSCAPE(16, 9),
        WIDE_PORTRAIT(9, 16),
        LONG_LANDSCAPE(2, 1),
        LONG_PORTRAIT(1, 2);

        public final int width;
        public final int height;

        AspectRatio(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public double ratio() {
            return (double) width / height;
        }
    }

    private final int id;
    private final Color colour;
    private final BufferedImage image;

    public Tile(BufferedImage image) {
        this.id = idCounter++;
        this.image = image;
        this.colour = ImageUtils.calculateAverageColourSquared(image);
    }

    public Tile(BufferedImage image, AspectRatio aspectRatio) {
        this.id = idCounter++;
        this.image = cropToAspectRatio(image, aspectRatio);
        this.colour = ImageUtils.calculateAverageColourSquared(this.image);
    }

    private Tile(BufferedImage image, int id, Color colour) {
        this.image = image;
        this.id = id;
        this.colour = colour;
    }

    private BufferedImage cropToAspectRatio(BufferedImage image, AspectRatio aspectRatio) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        int croppedWidth, croppedHeight;

        if (aspectRatio == AspectRatio.SQUARE) {
            croppedWidth = Math.min(originalWidth, originalHeight);
            croppedHeight = croppedWidth;
        } else {
            double desiredRatio = aspectRatio.ratio();
            double originalRatio = (double) originalWidth / originalHeight;

            if (Double.compare(originalRatio, desiredRatio) > 0) {
                croppedHeight = originalHeight;
                croppedWidth = (int) Math.round(desiredRatio * croppedHeight);
            } else if (Double.compare(originalRatio, desiredRatio) < 0) {
                croppedWidth = originalWidth;
                croppedHeight = (int) Math.round(croppedWidth / desiredRatio);
            } else {
                return image;
            }
        }
        BufferedImage croppedImage = new BufferedImage(croppedWidth, croppedHeight, image.getType());
        Graphics2D g = croppedImage.createGraphics();
        g.drawImage(
                image,
                0, 0,                                                                               // target top-left
                croppedWidth, croppedHeight,                                                        // target bott-right
                (originalWidth - croppedWidth) / 2, (originalHeight - croppedHeight) / 2,           // source top-left
                (originalWidth + croppedWidth) / 2, (originalHeight + croppedHeight) / 2,           // source bott-right
                null                                                                                // imageobserver
        );
        g.dispose();
        return croppedImage;
    }

    public Color getColour() {
        return colour;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Tile copy() {
        return new Tile(this.image, this.id, this.colour);
    }

    public int colourDistance(Color target) {
        int dR = this.colour.getRed() - target.getRed();
        int dG = this.colour.getGreen() - target.getGreen();
        int dB = this.colour.getBlue() - target.getBlue();
        int distanceSqr = (dR * dR) + (dG * dG) + (dB * dB);
        return (int) Math.sqrt(distanceSqr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile another)) return false;
        return this.id == another.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
