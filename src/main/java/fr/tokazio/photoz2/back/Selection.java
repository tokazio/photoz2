package fr.tokazio.photoz2.back;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Selection {

    private final List<Id> internal = new LinkedList<>();

    public boolean contains(final Id id) {
        if (id == null) {
            return false;
        }
        return internal.contains(id);
    }

    public boolean contains(final int id) {
        return contains(new Id(id));
    }

    public void clear() {
        internal.clear();
    }

    public void add(Id id) {
        if (id != null && !internal.contains(id)) {
            internal.add(id);
            Collections.sort(internal);
        }
    }

    public void remove(final Id id) {
        if (id != null) {
            internal.remove(id);
        }
    }

    public boolean isEmpty() {
        return internal.isEmpty();
    }

    public Id get(int i) {
        return internal.get(i);
    }

    public Id getFirst() {
        if (!internal.isEmpty()) {
            return get(0);
        }
        return null;
    }

    public Id getLast() {
        if (!internal.isEmpty()) {
            return get(internal.size() - 1);
        }
        return null;
    }

    @Override
    public String toString() {
        return internal.toString();
    }

    public List<Id> all() {
        return new LinkedList<>(internal);
    }

    public List<Id> reversed() {
        final List<Id> out = new ArrayList<>(internal);
        Collections.reverse(out);
        return out;
    }

    public int size() {
        return internal.size();
    }
}
