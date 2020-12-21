package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PictLoaderList {

    @JsonProperty
    private final List<PictLoader> internal = new ArrayList<>();

    public List<PictLoader> all() {
        return internal;
    }

    public void add(PictLoader pictLoader) {
        internal.add(pictLoader);
    }

    public int size() {
        return internal.size();
    }

    public PictLoader get(int i) {
        return internal.get(i);
    }

    public PictLoader load(int id, int w, int h) {
        final PictLoader p = internal.get(id);
        p.load(w, h);
        return p;
    }

    public void stopLoading() {
        for (PictLoader p : internal) {
            unload(p);
        }
    }

    public void unload(PictLoader pictLoader) {
        pictLoader.stopLoad();
    }

    public int pendingCount() {
        return getPending().size();
    }

    private List<PictLoader> getPending() {
        final List<PictLoader> out = new LinkedList<>();
        for (PictLoader p : internal) {
            if (p.isPending()) {
                out.add(p);
            }
        }
        return out;
    }

}
