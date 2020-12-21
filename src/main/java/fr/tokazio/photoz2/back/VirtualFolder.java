package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class VirtualFolder {

    final List<File> images = new LinkedList<>();
    @JsonProperty
    private final VirtualFolderList children = new VirtualFolderList();
    @JsonProperty
    private String name;
    @JsonProperty
    private String linkToFolder;
    @JsonIgnore
    private final List<VirtualFolderListener> listeners = new LinkedList<>();
    @JsonProperty
    private List<Img> pictures = new LinkedList<>();
    @JsonIgnore
    private VirtualFolder parent;//TODO le retrouver au load json

    private VirtualFolder() {
        //jackson
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

    public VirtualFolder(final @JsonProperty("name") String name, final @JsonProperty("linkToFolder") String linkToFolder) {
        this.name = name;
        this.linkToFolder = linkToFolder;
        if (linkToFolder != null && !linkToFolder.isEmpty()) {
            for (File f : new PictCollect(linkToFolder).all()) {
                pictures.add(new Img(linkToFolder, f));
            }
        }
    }

    public List<File> getImages() {
        if (images.isEmpty()) {
            for (Img img : pictures) {
                images.add(new File(linkToFolder + img.relative()));
            }
        }
        return images;
    }

    public String getFullName() {
        //TODO name from parent
        return name;
    }

    public void add(final List<File> selectedFiles) {
        for (File f : selectedFiles) {
            pictures.add(new Img(linkToFolder, f));
        }
        System.out.println(selectedFiles.size() + " files added to " + name);
    }
}
