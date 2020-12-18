package fr.tokazio.photoz2;

import fr.tokazio.filecollector.CollectorEngine;

import java.io.File;
import java.util.List;

public class PictCollect {

    public static final String ROOT = "C:\\Users\\rpetit.GROUPE-WELCOOP\\Downloads";

    public List<File> all() {
        return new CollectorEngine().dirFilter(d -> true).fileFilter(f -> {
            return true;
            //return f.toString().toUpperCase().endsWith(".PNG") || f.toString().toUpperCase().endsWith(".JPG");
        }).collect(ROOT);
    }
}
