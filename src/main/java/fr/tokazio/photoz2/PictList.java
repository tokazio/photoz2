package fr.tokazio.photoz2;

import java.util.ArrayList;
import java.util.List;

public class PictList {

    private final List<Pict> internal = new ArrayList<>();

    public List<Pict> all() {
        return internal;
    }

    public void add(Pict pict) {
        internal.add(pict);
    }

    public int size() {
        return internal.size();
    }

    public Pict get(int i) {
        return internal.get(i);
    }
}
