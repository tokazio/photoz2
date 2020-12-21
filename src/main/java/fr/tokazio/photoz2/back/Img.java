package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class Img {

    @JsonProperty
    private String relative;

    private Img() {
        //jackson
    }

    public Img(final String baseFolder, final File f) {
        this.relative = f.getAbsolutePath().replace(baseFolder, "");
    }

    public String relative() {
        return relative;
    }
}
