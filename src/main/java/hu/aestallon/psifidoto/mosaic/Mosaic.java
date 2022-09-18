package hu.aestallon.psifidoto.mosaic;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Mosaic {
    private final BufferedImage image;
    private final Set<Tile> tiles;

    private final ColourGrid colourGrid;
    private final ImageGrid imageGrid;

    public Mosaic(BufferedImage image, Set<Tile> tiles) {
        this.image = image;
        this.tiles = tiles;

        this.colourGrid = new ColourGrid();
        this.imageGrid = new ImageGrid();

        fillImageGridSimple();
    }

    // naive implementation
    private void fillImageGridSimple() {
        Set<Tile> availableTiles = new HashSet<>(tiles);
        for (int i = 0; i < colourGrid.grid.length; i++) {
            for (int j = 0; j < colourGrid.grid[0].length; j++) {
                final Color targetColour = colourGrid.grid[i][j];
                /*Tile bestMatchingTile = availableTiles.stream()
                        .min(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                        .orElseThrow(InsufficientTileException::new);
                System.out.println("Distance: " + bestMatchingTile.colourDistance(targetColour));*/
                List<Tile> bestMatching3Tiles = availableTiles.stream()
                        .sorted(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                        .limit(3)
                        .collect(Collectors.toList());
                if (bestMatching3Tiles.get(2).colourDistance(targetColour) <= 50) {
                    Collections.shuffle(bestMatching3Tiles);
                }
                imageGrid.grid[i][j] = bestMatching3Tiles.get(0).getImage();
                //availableTiles.remove(bestMatchingTile);
            }
        }
    }

    private void fillImageGridCenterBiased() {
        Set<Tile> availableTiles = new HashSet<>(tiles);
        final int centreX = colourGrid.grid.length / 2;
        final int centreY = colourGrid.grid[0].length / 2;

        Set<Color> visited = new HashSet<>();
        Queue<Color> queue = new ArrayDeque<>();

        queue.add(colourGrid.grid[centreX][centreY]);
        while (!queue.isEmpty()) {
            // TODO: incomplete.
        }
    }

    // naive implementation
    public BufferedImage export() throws InterruptedException, InvocationTargetException {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        System.out.println("Result image supposedly prepared");
        for (int i = 0; i < imageGrid.grid.length; i++) {
            for (int j = 0; j < imageGrid.grid[0].length; j++) {
                System.out.println("Treating image: " + (i * imageGrid.grid[0].length + j + 1) + " started...");
                BufferedImage tileImage = imageGrid.grid[i][j];
                BufferedImage resizedTileImage = new BufferedImage(colourGrid.tileWidth, colourGrid.tileHeight, tileImage.getType());
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D tileG = resizedTileImage.createGraphics();
                    tileG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    tileG.drawImage(tileImage, 0, 0, colourGrid.tileWidth, colourGrid.tileHeight, 0, 0, tileImage.getWidth(), tileImage.getHeight(), null);
                    tileG.dispose();
                });
//                Thread.sleep(2_000L);
                int finalJ = j;
                int finalI = i;
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D resultGraphics = result.createGraphics();
                    resultGraphics.drawImage(resizedTileImage, null, finalJ * colourGrid.tileWidth, finalI * colourGrid.tileHeight);
//                    resultGraphics.dispose();
                });
//                Thread.sleep(2_000L);
                System.out.println("Treating image: " + (i * imageGrid.grid[0].length + j + 1) + " completed.");
            }
        }
        return result;
    }

    private class ColourGrid {
        private final int tileHeight;
        private final int tileWidth;
        private final Color[][] grid;

        public ColourGrid() {
            int[] rowsAndCols = calculateRowsAndCols(/*Mosaic.this.tiles.size()*/2_000);
            int rows = rowsAndCols[0];
            int cols = rowsAndCols[1];
            this.grid = new Color[rows][cols];

            this.tileHeight = image.getHeight() / rows;
            this.tileWidth = image.getWidth() / cols;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // Boundaries
                    int x0 = j * tileWidth;
                    int x1 = x0 + tileWidth;
                    int y0 = i * tileHeight;
                    int y1 = y0 + tileHeight;
                    grid[i][j] = ImageUtils.calculateAverageColourOfRegion(image, x0, x1, y0, y1);
                }
            }
        }

        private int[] calculateRowsAndCols(int tileCount) {
            int factor = (int) Math.sqrt(tileCount);
            while (tileCount % factor != 0) factor--;
            //return new int[]{factor, tileCount / factor};
            return new int[]{tileCount / factor, factor};
        }
    }

    private class ImageGrid {
        private final BufferedImage[][] grid;

        private ImageGrid() {
            this.grid = new BufferedImage[colourGrid.grid.length][colourGrid.grid[0].length];
        }
    }

    private static class Coordinate<T> {
        private final int x;
        private final int y;
        private final T t;

        private Coordinate(int x, int y, T t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }
    }

}