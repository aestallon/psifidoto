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
//        fillImageGridCenterBiased();
    }

    // naive implementation
    private void fillImageGridSimple() {
        Set<Tile> availableTiles = new HashSet<>(tiles);
        for (int i = 0; i < colourGrid.rowCount; i++) {
            for (int j = 0; j < colourGrid.columnCount; j++) {
                final Color targetColour = colourGrid.getColourAt(j, i);
                /*Tile bestMatchingTile = availableTiles.stream()
                        .min(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                        .orElseThrow(InsufficientTileException::new);
                System.out.println("Distance: " + bestMatchingTile.colourDistance(targetColour));*/
                List<Tile> bestMatching3Tiles = availableTiles.stream()
                        .sorted(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                        .limit(3)
                        .collect(Collectors.toList());
                if (bestMatching3Tiles.get(bestMatching3Tiles.size() - 1).colourDistance(targetColour) <= 50) {
                    Collections.shuffle(bestMatching3Tiles);
                }
                imageGrid.grid.add(new Coordinate<>(j, i, bestMatching3Tiles.get(0).getImage()));
                availableTiles.remove(bestMatching3Tiles.get(0));
            }
        }
    }

    private void fillImageGridCenterBiased() {
        Set<Tile> availableTiles = new HashSet<>(tiles);
        final int centreX = colourGrid.columnCount / 2;
        final int centreY = colourGrid.rowCount / 2;

        Set<Coordinate<Color>> visited = new TreeSet<>();
        Queue<Coordinate<Color>> queue = new ArrayDeque<>();

        Coordinate<Color> root = colourGrid.get(centreX, centreY);
        visited.add(root);
        queue.add(root);

        while (!queue.isEmpty()) {
            Coordinate<Color> colourCoord = queue.poll();
            Color targetColour = colourCoord.t;
            Tile bestMatchingTile = availableTiles.stream()
                    .min(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                    .orElseThrow(InsufficientTileException::new);

            imageGrid.grid.add(new Coordinate<>(
                    colourCoord.x, colourCoord.y, bestMatchingTile.getImage()
            ));
            availableTiles.remove(bestMatchingTile);
            for (Coordinate<Color> neighbour : colourGrid.neighboursOf(colourCoord)) {
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
        for (int i = 0; i < colourGrid.rowCount; i++) {
            for (int j = 0; j < colourGrid.columnCount; j++) {
                System.out.println("Treating image: " + (i * colourGrid.columnCount + j + 1) + " started...");
                BufferedImage tileImage = imageGrid.getImageAt(j, i);
                BufferedImage resizedTileImage = new BufferedImage(colourGrid.tileWidth, colourGrid.tileHeight, tileImage.getType());
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D tileG = resizedTileImage.createGraphics();
                    tileG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    tileG.drawImage(tileImage, 0, 0, colourGrid.tileWidth, colourGrid.tileHeight, 0, 0, tileImage.getWidth(), tileImage.getHeight(), null);
                    tileG.dispose();
                });
                int finalJ = j;
                int finalI = i;
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D resultGraphics = result.createGraphics();
                    resultGraphics.drawImage(resizedTileImage, null, finalJ * colourGrid.tileWidth, finalI * colourGrid.tileHeight);
//                    resultGraphics.dispose();
                });
                System.out.println("Treating image: " + (i * colourGrid.columnCount + j + 1) + " completed.");
            }
        }
        return result;
    }

    private class ColourGrid {
        private final int tileHeight;
        private final int tileWidth;
        private final int rowCount;
        private final int columnCount;

        private final SortedSet<Coordinate<Color>> grid;

        public ColourGrid() {
            int[] rowsAndCols = calculateRowsAndCols(Mosaic.this.tiles.size()/*3_000*/);
            this.rowCount = rowsAndCols[0];
            this.columnCount = rowsAndCols[1];
            this.grid = new TreeSet<>();

            this.tileHeight = image.getHeight() / rowCount;
            this.tileWidth = image.getWidth() / columnCount;
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    // Boundaries
                    int x0 = j * tileWidth;
                    int x1 = x0 + tileWidth;
                    int y0 = i * tileHeight;
                    int y1 = y0 + tileHeight;
                    Coordinate<Color> colourCoordinate = new Coordinate<>(
                            j, i, ImageUtils.calculateAverageColourOfRegion(image, x0, x1, y0, y1)
                    );
                    grid.add(colourCoordinate);
                }
            }
        }

        private int[] calculateRowsAndCols(int tileCount) {
            int factor = (int) Math.sqrt(tileCount);
            while (tileCount % factor != 0) factor--;
            return new int[]{factor, tileCount / factor};
//            return new int[]{tileCount / factor, factor};
        }

        private Coordinate<Color> get(int x, int y) {
            return grid.stream().filter(coord -> coord.x == x && coord.y == y)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }

        private Color getColourAt(int x, int y) {
            return grid.stream().filter(coord -> coord.x == x && coord.y == y)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new)
                    .t;
        }

        private SortedSet<Coordinate<Color>> neighboursOf(Coordinate<Color> colorCoordinate) {
            return grid.stream()
                    .filter(
                            c -> c.x - colorCoordinate.x <= 1 && c.x - colorCoordinate.x >= -1 &&
                                 c.y - colorCoordinate.y <= 1 && c.y - colorCoordinate.y >= -1 &&
                                 !c.equals(colorCoordinate)
                    )
                    .collect(Collectors.toCollection(TreeSet::new));
        }

    }

    private static class ImageGrid {
        private final SortedSet<Coordinate<BufferedImage>> grid;

        private ImageGrid() {
            this.grid = new TreeSet<>();
        }

        private boolean add(Coordinate<BufferedImage> imageCoordinate) {
            return grid.add(imageCoordinate);
        }

        private boolean contains(Coordinate<BufferedImage> imageCoordinate) {
            return grid.contains(imageCoordinate);
        }

        private SortedSet<Coordinate<BufferedImage>> neighboursOf(Coordinate<BufferedImage> imageCoordinate) {
            return grid.stream()
                    .filter(
                            c -> c.x - imageCoordinate.x <= 1 && c.x - imageCoordinate.x >= -1 &&
                                 c.y - imageCoordinate.y <= 1 && c.y - imageCoordinate.y >= -1 &&
                                 !c.equals(imageCoordinate)
                    )
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        private Coordinate<BufferedImage> get(int x, int y) {
            return grid.stream().filter(coord -> coord.x == x && coord.y == y)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }

        private BufferedImage getImageAt(int x, int y) {
            return grid.stream().filter(coord -> coord.x == x && coord.y == y)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new)
                    .t;
        }
    }

    private static class Coordinate<T> implements Comparable<Coordinate<T>> {
        private final int x;
        private final int y;
        private final T t;

        private Coordinate(int x, int y, T t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Coordinate<?> another)) return false;
            return this.x == another.x &&
                   this.y == another.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public int compareTo(Coordinate<T> another) {
            int dX = this.x - another.x;
            return (dX == 0)
                    ? this.y - another.y
                    : dX;
        }
    }

}