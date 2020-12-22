package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class VirtualFolder {

    @JsonProperty
    private final VirtualFolderList children = new VirtualFolderList();
    @JsonProperty
    private String name;
    @JsonProperty
    private String linkToFolder;
    @JsonProperty
    private PictLoaderList pictures = new PictLoaderList();
    @JsonIgnore
    private VirtualFolder parent;
    @JsonIgnore
    private boolean fresh = true;

    private VirtualFolder() {
        //jackson
    }

    public void add(final VirtualFolder virtualFolder) {
        virtualFolder.parent = this;
        children.add(virtualFolder);
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


    public boolean hasAParent() {
        return parent != null;
    }

    public VirtualFolder(final String name, final String linkToFolder) {
        this.name = name;
        this.linkToFolder = linkToFolder;
        if (linkToFolder != null && !linkToFolder.isEmpty()) {
            int k = 0;
            for (File f : new PictCollect(linkToFolder).all()) {
                pictures.add(new PictLoader(k++, f));
            }
        }
    }

    public PictLoaderList getPictures() {
        return pictures;
    }

    public String getFullName() {
        //TODO name from parent
        return name;
    }

    public void add(final PictLoaderList selectedFiles) {
        pictures.add(selectedFiles);
        System.out.println(selectedFiles.size() + " files added to " + name);
    }

    void setParent(VirtualFolder parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return name;
    }

    public VirtualFolder getParent() {
        return parent;
    }

    public boolean remove(VirtualFolder vf) {
        return children.remove(vf);
    }

    public boolean isFresh() {
        return fresh;
    }

    public void setNotFresh() {
        fresh = false;
    }
}
