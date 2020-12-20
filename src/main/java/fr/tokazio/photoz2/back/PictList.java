package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PictList {

    @JsonProperty
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

    public Pict load(int id, int w, int h) {
        final Pict p = internal.get(id);
        p.load(w, h);
        return p;
    }

    public void stopLoading() {
        for (Pict p : internal) {
            unload(p);
        }
    }

    public void unload(Pict pict) {
        pict.stopLoad();
    }

    public int pendingCount() {
        return getPending().size();
    }

    private List<Pict> getPending() {
        final List<Pict> out = new LinkedList<>();
        for (Pict p : internal) {
            if (p.isPending()) {
                out.add(p);
            }
        }
        return out;
    }

}
