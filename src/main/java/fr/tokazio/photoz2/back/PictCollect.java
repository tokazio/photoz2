package fr.tokazio.photoz2.back;

import fr.tokazio.filecollector.CollectorEngine;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PictCollect {

    public static final String ROOT = "/users/romain/desktop";//"C:\\Users\\rpetit.GROUPE-WELCOOP\\Downloads";

    private static final String[] EXTS = {"PNG", "JPG", "JPEG"};

    public List<File> all() {
        return new CollectorEngine().dirFilter(d -> true).fileFilter(f -> Arrays.stream(EXTS).anyMatch(f.toString().toUpperCase()::endsWith)).collect(ROOT);
    }
}
