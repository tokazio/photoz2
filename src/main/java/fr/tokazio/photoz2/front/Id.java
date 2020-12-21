package fr.tokazio.photoz2.front;

import java.util.Objects;

public class Id {

    private final int id;

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
}
