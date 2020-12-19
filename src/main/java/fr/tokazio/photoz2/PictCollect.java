package fr.tokazio.photoz2;

import fr.tokazio.filecollector.CollectorEngine;

import java.io.File;
import java.util.List;

public class PictCollect {

    public static final String ROOT = "/users/romain/desktop";//"C:\\Users\\rpetit.GROUPE-WELCOOP\\Downloads";

    int nb = 0;

    public List<File> all() {
        return new CollectorEngine().dirFilter(d -> true).fileFilter(f -> {
            nb++;
            //TODO Limit in CollectorAPI
            /*if(nb>7*10){
                return false;
            }
             */
            //return true;
            return f.toString().toUpperCase().endsWith(".PNG") || f.toString().toUpperCase().endsWith(".JPG");
        }).collect(ROOT);
    }
}
