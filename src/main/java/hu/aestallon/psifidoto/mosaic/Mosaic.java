package hu.aestallon.psifidoto.mosaic;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Mosaic {

    public static MosaicBuilder newInstance() {
        return new MosaicBuilder();
    }

    private final BufferedImage image;
    private final Set<Tile> tiles;

    private final Grid<Color> colourGrid;
    private final Grid<Tile> tileGrid;

    private final int tileHeight;
    private final int tileWidth;
    private final int rowCount;
    private final int columnCount;

    public Mosaic(BufferedImage targetImage,
                  Stream<BufferedImage> tileImages,
                  int tileCount,
                  int minimumRepetitionDistance,
                  Tile.AspectRatio tileAspectRatio) {
        this.image = targetImage;

        int[] rowsAndCols = calculateRowsAndCols(tileCount, tileAspectRatio);
        this.columnCount = rowsAndCols[0];
        this.rowCount = rowsAndCols[1];

        this.tileHeight = image.getHeight() / rowCount;
        this.tileWidth = (int) Math.round(tileHeight * tileAspectRatio.ratio());

        this.tiles = tileImages
                .map(bi -> new Tile(bi, tileAspectRatio))
                .collect(Collectors.toSet());

        this.colourGrid = new Grid<>(columnCount, rowCount);
        fillColourGrid();

        this.tileGrid = new Grid<>(columnCount, rowCount);
        fillImageGridCenterBiased(minimumRepetitionDistance);
    }

    // used by builder
    Mosaic(Supplier<BufferedImage> targetImageSupplier,
           Supplier<Set<Tile>> tileSupplier,
           Tile.AspectRatio tileAspectRatio,
           int tileCountHint,
           int minRepetitionDistance) {
        this.image = targetImageSupplier.get();

        int[] rowsAndCols = calculateRowsAndCols(tileCountHint, tileAspectRatio);
        this.columnCount = rowsAndCols[0];
        this.rowCount = rowsAndCols[1];

        this.tileHeight = image.getHeight() / rowCount;
        this.tileWidth = (int) Math.round(tileHeight * tileAspectRatio.ratio());

        this.tiles = tileSupplier.get();

        this.colourGrid = new Grid<>(columnCount, rowCount);
        fillColourGrid();

        this.tileGrid = new Grid<>(columnCount, rowCount);
        fillImageGridCenterBiased(minRepetitionDistance);
    }

    private int[] calculateRowsAndCols(int tileCount, Tile.AspectRatio aspectRatio) {
        assert image != null;

        double imageRatio = (double) image.getWidth() / image.getHeight();
        double tileRatio = aspectRatio.ratio();
        int side1 = (int) Math.round(Math.sqrt(tileRatio * tileCount / imageRatio));
        int side2 = (int) Math.round(Math.sqrt(imageRatio * tileCount / tileRatio));
        System.out.println("Actual tile count = " + (side1 * side2));
        // TODO: the algorithm works almost correctly with this line commented out.
        //  An issue is with laying out portrait tiles: in this orientation the
        //  tiles are always more elongated then they should be.
        /*if (Double.compare(imageRatio / tileRatio, 1d) >= 0) {
            return new int[]{side1, side2};
        } else*/ return new int[]{side2, side1};
    }

    private void fillColourGrid() {
        final int scanWidth  = image.getWidth() / columnCount;
        final int scanHeight = image.getHeight() / rowCount;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                colourGrid.add(j, i, ImageUtils.calculateAverageColourSquared(
                        image.getSubimage(j * scanWidth, i * scanHeight, scanWidth, scanHeight)
                ));
            }
        }
    }

    @Deprecated
    @SuppressWarnings("unused")
    private void fillImageGridSimple() {
        Set<Tile> availableTiles = new HashSet<>(tiles);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                final Color targetColour = colourGrid.get(j, i).unwrap();
                List<Tile> bestMatching3Tiles = availableTiles.stream()
                        .sorted(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                        .limit(3)
                        .collect(Collectors.toList());
                if (bestMatching3Tiles
                            .get(bestMatching3Tiles.size() - 1)
                            .colourDistance(targetColour) <= 50) {
                    Collections.shuffle(bestMatching3Tiles);
                }
                tileGrid.add(j, i, bestMatching3Tiles.get(0));
                availableTiles.remove(bestMatching3Tiles.get(0));
            }
        }
    }

    private void fillImageGridCenterBiased(int minimumRepetitionDistance) {
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

            Tile bestMatchingTile;
            if (minimumRepetitionDistance == 0) {
                bestMatchingTile = availableTiles.stream()
                        .min(Comparator.comparingInt(tile -> tile.colourDistance(targetColour)))
                        .orElseThrow(InsufficientTileException::new);
            } else {
                Stack<Tile> tileStack = availableTiles.stream()
                        .sorted(Comparator.comparingInt((Tile tile) -> tile.colourDistance(targetColour)).reversed())
                        .collect(Collectors.toCollection(Stack::new));
                do {
                    bestMatchingTile = tileStack.pop();
                } while (
                        !tileStack.empty() &&
                        distanceToSameImageOnGrid(bestMatchingTile, colourCoord.x(), colourCoord.y()) < minimumRepetitionDistance
                );
            }

            tileGrid.add(colourCoord.x(), colourCoord.y(), bestMatchingTile);
            for (Grid.Coordinate<Color> neighbour : colourGrid.neighboursOf(colourCoord)) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
    }

    private int distanceToSameImageOnGrid(Tile tile, int placementX, int placementY) {
        SortedSet<Grid.Coordinate<Tile>> neighbours = tileGrid.neighboursOf(placementX, placementY);

        Set<Grid.Coordinate<Tile>> visited = new TreeSet<>(neighbours);
        Queue<Grid.Coordinate<Tile>> queue = new ArrayDeque<>(neighbours);

        while (!queue.isEmpty()) {
            Grid.Coordinate<Tile> c = queue.poll();
            if (c.unwrap().equals(tile)) {
                int dX = placementX - c.x();
                int dY = placementY - c.y();
                return (int) Math.round(Math.sqrt(dX*dX + dY*dY));
            }
            for (var neighbour : tileGrid.neighboursOf(c)) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    // naive implementation
    public BufferedImage export() throws InterruptedException, InvocationTargetException {
        final int resultWidth = tileWidth * columnCount;
        final int resultHeight = tileHeight * rowCount;
        BufferedImage result = new BufferedImage(resultWidth, resultHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, resultWidth, resultHeight);
        g.dispose();
        System.out.println("Result image supposedly prepared");
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                BufferedImage tileImage = tileGrid.get(j, i).unwrap().getImage();
                BufferedImage resizedTileImage = new BufferedImage(tileWidth, tileHeight, tileImage.getType());
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D tileG = resizedTileImage.createGraphics();
                    tileG.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR
                    );
                    tileG.drawImage(
                            tileImage,
                            0, 0, tileWidth, tileHeight,
                            0, 0, tileImage.getWidth(), tileImage.getHeight(),
                            null
                    );
                    tileG.dispose();
                });
                int finalJ = j;
                int finalI = i;
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D resultGraphics = result.createGraphics();
                    resultGraphics.drawImage(
                            resizedTileImage, null,
                            finalJ * tileWidth, finalI * tileHeight
                    );
                    resultGraphics.dispose();
                });
            }
        }
        return result;
    }
}