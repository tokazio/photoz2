package fr.tokazio.photoz2.back;

import java.util.Objects;

public class Id implements Comparable {

    private final Integer id;

    public Id(final int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id1 = (Id) o;
        return id == id1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + "";
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;
        if (o == null || getClass() != o.getClass()) return 1;
        Id id1 = (Id) o;
        return id.compareTo(id1.id);
    }

    public int asInt() {
        return id;
    }
}
