package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class VirtualFolderSerializer {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static VirtualFolderSerializer instance;

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        mapper.setVisibility(mapper.getDeserializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public static VirtualFolderSerializer getInstance() {
        if (instance == null) {
            instance = new VirtualFolderSerializer();
        }
        return instance;
    }

    public VirtualFolder load(final String filename) throws IOException {
        if (filename == null) {
            throw new IllegalArgumentException("Can't load a null file");
        }
        final File f = new File(filename);
        if (!f.exists()) {
            return new VirtualFolder("Tous", null);
//            throw new FileNotFoundException("Can't load '" + filename + "' because it not exists");
        }
        final VirtualFolder root = mapper.readValue(f, VirtualFolder.class);
        //This wil place the parent field of each children
        subLoad(root);
        return root;
    }

    private void subLoad(VirtualFolder parent) {
        int k = 0;
        for (PictLoader pl : parent.getPictures().all()) {
            pl.changeId(k++);
        }
        for (VirtualFolder vf : parent.getChildren().all()) {
            vf.setParent(parent);
            subLoad(vf);
        }
    }

    public void save(final VirtualFolder virtualFolder, final String filename) throws IOException {
        if (virtualFolder.hasAParent()) {
            //TODO save root (parent==null)
            throw new UnsupportedOperationException("Can't save a non root virtual folder");
        }
        mapper.writeValue(new File(filename), virtualFolder);
    }

}
