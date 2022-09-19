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

    private final Grid<Color> colourGrid;
    private final Grid<BufferedImage> imageGrid;

    private final int tileHeight;
    private final int tileWidth;
    private final int rowCount;
    private final int columnCount;

    public Mosaic(BufferedImage image, Set<Tile> tiles) {
        this.image = image;
        this.tiles = tiles;

        int[] rowsAndCols = calculateRowsAndCols(Mosaic.this.tiles.size()/*3_000*/);
        this.rowCount = rowsAndCols[0];
        this.columnCount = rowsAndCols[1];

        this.tileHeight = image.getHeight() / rowCount;
        this.tileWidth = image.getWidth() / columnCount;

        this.colourGrid = new Grid<>(columnCount, rowCount);
        fillColourGrid();

        this.imageGrid = new Grid<>(columnCount, rowCount);
        fillImageGridSimple();
//        fillImageGridCenterBiased();
    }

    private int[] calculateRowsAndCols(int tileCount) {
        int factor = (int) Math.sqrt(tileCount);
        while (tileCount % factor != 0) factor--;
        return new int[]{factor, tileCount / factor};
//            return new int[]{tileCount / factor, factor};
    }

    private void fillColourGrid() {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                // Boundaries of observed region:
                int x0 = j * tileWidth;
                int x1 = x0 + tileWidth;
                int y0 = i * tileHeight;
                int y1 = y0 + tileHeight;
                colourGrid.add(j, i, ImageUtils.calculateAverageColourOfRegion(image, x0, x1, y0, y1));
            }
        }
    }


    // naive implementation
    private void fillImageGridSimple() {
        Set<Tile> availableTiles = new HashSet<>(tiles);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                final Color targetColour = colourGrid.get(j, i).unwrap();
                List<Tile> bestMatching3Tiles = availableTiles.stream()
                        .sorted(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                        .limit(3)
                        .collect(Collectors.toList());
                if (bestMatching3Tiles.get(bestMatching3Tiles.size() - 1).colourDistance(targetColour) <= 50) {
                    Collections.shuffle(bestMatching3Tiles);
                }
                imageGrid.add(j, i, bestMatching3Tiles.get(0).getImage());
                availableTiles.remove(bestMatching3Tiles.get(0));
            }
        }
    }

    private void fillImageGridCenterBiased() {
        Set<Tile> availableTiles = new HashSet<>(tiles);
        final int centreX = columnCount / 2;
        final int centreY = rowCount / 2;

        Set<Grid.Coordinate<Color>> visited = new TreeSet<>();
        Queue<Grid.Coordinate<Color>> queue = new ArrayDeque<>();

        Grid.Coordinate<Color> root = colourGrid.get(centreX, centreY);
        visited.add(root);
        queue.add(root);

        while (!queue.isEmpty()) {
            Grid.Coordinate<Color> colourCoord = queue.poll();
            Color targetColour = colourCoord.unwrap();
            Tile bestMatchingTile = availableTiles.stream()
                    .min(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                    .orElseThrow(InsufficientTileException::new);

            imageGrid.add(colourCoord.x(), colourCoord.y(), bestMatchingTile.getImage());
            availableTiles.remove(bestMatchingTile);
            for (Grid.Coordinate<Color> neighbour : colourGrid.neighboursOf(colourCoord)) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
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
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                System.out.println("Treating image: " + (i * columnCount + j + 1) + " started...");
                BufferedImage tileImage = imageGrid.get(j, i).unwrap();
                BufferedImage resizedTileImage = new BufferedImage(tileWidth, tileHeight, tileImage.getType());
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D tileG = resizedTileImage.createGraphics();
                    tileG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    tileG.drawImage(tileImage, 0, 0, tileWidth, tileHeight, 0, 0, tileImage.getWidth(), tileImage.getHeight(), null);
                    tileG.dispose();
                });
                int finalJ = j;
                int finalI = i;
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D resultGraphics = result.createGraphics();
                    resultGraphics.drawImage(resizedTileImage, null, finalJ * tileWidth, finalI * tileHeight);
//                    resultGraphics.dispose();
                });
                System.out.println("Treating image: " + (i * columnCount + j + 1) + " completed.");
            }
        }
        return result;
    }
}