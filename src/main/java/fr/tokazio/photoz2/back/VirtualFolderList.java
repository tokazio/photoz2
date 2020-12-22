package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class VirtualFolderList {

    @JsonProperty
    private final List<VirtualFolder> internal = new LinkedList<>();

    public void add(VirtualFolder virtualFolder) {
        internal.add(virtualFolder);
    }

    public List<VirtualFolder> all() {
        return internal;
    }

    public int size() {
        return internal.size();
    }


    public boolean remove(VirtualFolder vf) {
        return internal.remove(vf);
    }
}
