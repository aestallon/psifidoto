package hu.aestallon.psifidoto.util.data;

import java.util.Objects;

public final class Pair<A, B> {

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    private final A a;
    private final B b;

    private Pair(A a, B b) {
        this.a = Objects.requireNonNull(a);
        this.b = Objects.requireNonNull(b);
    }

    public A a() { return a; }
    public B b() { return b; }
}
