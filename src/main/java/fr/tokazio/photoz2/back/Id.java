package fr.tokazio.photoz2.back;

import java.util.Objects;

public class Id implements Comparable<Id> {

    private final Integer value;

    public Id(final int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id1 = (Id) o;
        return value == id1.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value + "";
    }

    @Override
    public int compareTo(Id o) {
        if (this == o) return 0;
        if (o == null || getClass() != o.getClass()) return 1;
        return value.compareTo(o.value);
    }

    public int asInt() {
        return value;
    }
}
