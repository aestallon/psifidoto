package hu.aestallon.psifidoto.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    private final Color colour;
    private final BufferedImage image;

    public Tile(BufferedImage image) {
        this.image = image;
        this.colour = ImageUtils.calculateAverageColourSquared(image);
    }

    public Color getColour() {
        return colour;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int colourDistance(Color target) {
        int dR = this.colour.getRed() - target.getRed();
        int dG = this.colour.getGreen() - target.getGreen();
        int dB = this.colour.getBlue() - target.getBlue();
        int distanceSqr = (dR * dR) + (dG * dG) + (dB * dB);
        return (int) Math.sqrt(distanceSqr);
    }
}
