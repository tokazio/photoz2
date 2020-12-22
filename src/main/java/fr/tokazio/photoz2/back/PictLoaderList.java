package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PictLoaderList {

    @JsonProperty
    private List<PictLoader> internal = new ArrayList<>();

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

    public void move(final Selection selection, int dragTo) {
        if (dragTo < 0) {
            return;
        }
        if (dragTo > selection.getLast().asInt()) {
            dragTo -= selection.size();
        }
        final List<PictLoader> copy = new ArrayList<>(internal.size());
        for (PictLoader pict : internal) {
            if (!selection.contains(pict.getId())) {
                copy.add(pict);
            }
        }
        for (Id id : selection.reversed()) {
            copy.add(dragTo, internal.get(id.asInt()));
        }
        //re compute ids
        int i = 0;
        for (PictLoader pict : copy) {
            pict.changeId(i++);
        }
        internal.clear();
        internal = copy;
    }

    public void clear() {
        internal.clear();
    }

    public void remove(final Selection selection) {
        final List<PictLoader> copy = new ArrayList<>(internal.size());
        for (PictLoader pict : internal) {
            if (!selection.contains(pict.getId())) {
                copy.add(pict);
            }
        }
        //re compute ids
        int i = 0;
        for (PictLoader pict : copy) {
            pict.changeId(i++);
        }
        internal.clear();
        internal = copy;
    }

    public void add(final PictLoaderList others) {
        int k = internal.size();
        for (PictLoader pl : others.all()) {
            internal.add(pl);
            pl.changeId(k++);
        }
    }
}
