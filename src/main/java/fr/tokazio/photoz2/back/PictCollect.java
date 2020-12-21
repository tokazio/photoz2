package fr.tokazio.photoz2.back;

import fr.tokazio.filecollector.CollectorEngine;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PictCollect {

    private static final String[] EXTS = {"PNG", "JPG", "JPEG"};

    private final String folder;

    public PictCollect(String folder) {
        this.folder = folder;
    }

    public List<File> all() {
        return new CollectorEngine().dirFilter(d -> true).fileFilter(f -> Arrays.stream(EXTS).anyMatch(f.toString().toUpperCase()::endsWith)).collect(folder);
    }
}
