package hu.aestallon.psifidoto.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    private static int idCounter = 0;

    private final int id;
    private final Color colour;
    private final BufferedImage image;

    public Tile(BufferedImage image) {
        this.id = idCounter++;
        this.image = image;
        this.colour = ImageUtils.calculateAverageColourSquared(image);
    }

    private Tile(BufferedImage image, int id, Color colour) {
        this.image = image;
        this.id = id;
        this.colour = colour;
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
