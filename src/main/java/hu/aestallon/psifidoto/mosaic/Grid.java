package hu.aestallon.psifidoto.mosaic;

import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class Grid<E> {
    private final int width;
    private final int height;
    private final SortedSet<Coordinate<E>> coordinates;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        coordinates = new TreeSet<>();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean add(int x, int y, E e) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Illegal coordinates!");
        }
        if (e == null) throw new NullPointerException("Cannot add null element to grid!");
        return coordinates.add(new Coordinate<>(x, y, e));
    }

    public Coordinate<E> get(int x, int y) {
        return coordinates.stream().filter(c -> c.x == x && c.y == y)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public SortedSet<Coordinate<E>> neighboursOf(Coordinate<E> coord) {
        return coordinates.stream()
                .filter(
                        c -> c.x - coord.x <= 1 && c.x - coord.x >= -1 &&
                             c.y - coord.y <= 1 && c.y - coord.y >= -1 &&
                             !c.equals(coord)
                )
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public SortedSet<Coordinate<E>> neighboursOf(int x, int y) {
        return coordinates.stream()
                .filter(
                        c -> c.x - x <= 1 && c.x - x >= -1 &&
                             c.y - y <= 1 && c.y - y >= -1 &&
                             !(c.x == x && c.y == y)
                )
                .collect(Collectors.toCollection(TreeSet::new));
    }

    static class Coordinate<E> implements Comparable<Coordinate<E>> {
        private final int x;
        private final int y;
        private final E e;

        private Coordinate(int x, int y, E e) {
            this.x = x;
            this.y = y;
            this.e = e;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public E unwrap() {
            return e;
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
        public int compareTo(Coordinate<E> another) {
            int dX = this.x - another.x;
            return (dX == 0)
                    ? this.y - another.y
                    : dX;
        }
    }
}
