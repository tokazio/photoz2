package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class VirtualFolder {

    @JsonProperty
    private final String name;
    @JsonProperty
    private final VirtualFolderList children = new VirtualFolderList();
    @JsonProperty
    private final PictLoaderList pictures = new PictLoaderList();
    @JsonIgnore
    private final List<VirtualFolderListener> listeners = new LinkedList<>();
    @JsonIgnore
    private VirtualFolder parent;

    @JsonCreator
    public VirtualFolder(final @JsonProperty("name") String name) {
        this.name = name;
    }

    public void add(final VirtualFolder virtualFolder) {
        virtualFolder.parent = this;
        children.add(virtualFolder);
        for (VirtualFolderListener l : listeners) {
            l.onAdded(virtualFolder);
        }
    }

    public VirtualFolder getParent() {
        return parent;
    }

    public void add(final PictLoader pictLoader) {
        if (pictLoader != null) {
            pictures.add(pictLoader);
        }
    }

    public String getName() {
        return name;
    }

    public VirtualFolderList getChildren() {
        return children;
    }

    public int getChildCount() {
        return children.size();
    }

    public void addListener(final VirtualFolderListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }


    public boolean hasAParent() {
        return parent != null;
    }
}
