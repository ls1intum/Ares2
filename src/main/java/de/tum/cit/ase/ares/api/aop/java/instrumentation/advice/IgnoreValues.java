package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class IgnoreValues {

    public static final IgnoreValues NONE = new IgnoreValues(Type.NONE);

    public static final IgnoreValues ALL = new IgnoreValues(Type.ALL);

    public enum Type {
        NONE,
        NONE_EXCEPT,
        ALL,
        ALL_EXCEPT
    }

    private final Type type;

    private final Integer index;

    private IgnoreValues(Type type) {
        this(type, null);
    }

    private IgnoreValues(@Nonnull Type type, Integer index) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.index = index;
    }

    public static IgnoreValues noneExcept(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }

        return new IgnoreValues(Type.NONE_EXCEPT, index);
    }

    public static IgnoreValues allExcept(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }

        return new IgnoreValues(Type.ALL_EXCEPT, index);
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

}
