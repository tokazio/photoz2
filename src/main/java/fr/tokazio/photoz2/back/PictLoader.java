package fr.tokazio.photoz2.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PictLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PictLoader.class);


    @JsonIgnore
    private final List<LoadedListener> loadedListeners = new LinkedList<>();
    @JsonIgnore
    private final List<ProgressListener> progressListeners = new LinkedList<>();
    @JsonIgnore
    private int id;
    @JsonIgnore
    private Exception error;
    @JsonIgnore
    private int w = 1;
    @JsonIgnore
    private int h = 1;
    @JsonIgnore
    private float progress = 0;
    @JsonIgnore
    private volatile State state = State.NONE;
    @JsonIgnore
    private PictWorker sw1;
    @JsonIgnore
    private Image image;
    @JsonProperty
    private String file;

    private PictLoader() {
        //jackson
    }

    public PictLoader(final int id, final File file) {
        this.id = id;
        this.file = file.getAbsolutePath();
    }

    public int getId() {
        return id;
    }

    public float getProgress() {
        return progress;
    }

    @JsonIgnore
    public boolean isPending() {
        //TODO detecte pending timeout and put error
        return State.PENDING.equals(state) || State.LOADING.equals(state);
    }

    public boolean hasError() {
        return State.ERROR.equals(state);
    }

    public void load(int w, int h) {
        this.w = w;
        this.h = h;
        if (!isLoaded() && !isLoading()) {
            if (sw1 == null) {
                sw1 = new PictWorker(this);
            }
            state = State.PENDING;
            sw1.execute();
        }
    }

    @JsonIgnore
    public boolean isLoaded() {
        return State.LOADED.equals(state) || State.ERROR.equals(state);
    }

    private void fireLoaded() {
        for (LoadedListener l : loadedListeners) {
            l.onLoaded(this);
        }
    }

    private void fireProgress(float percent) {
        for (ProgressListener l : progressListeners) {
            l.onProgress(this, percent);
        }
    }

    public String getExt() {
        final File f = new File(file);
        return f.getName().substring(f.getName().lastIndexOf('.')).toLowerCase();
    }

    public PictLoader addLoadedListener(LoadedListener loadedListener) {
        if (loadedListener != null) {
            loadedListeners.add(loadedListener);
        }
        return this;
    }

    public Image asImage() {
        return image;
    }

    public PictLoader addProgressListener(ProgressListener progressListener) {
        if (progressListener != null) {
            progressListeners.add(progressListener);
        }
        return this;
    }

    public void stopLoad() {
        if (State.ERROR.equals(state)) {
            return;
        }
        if (isLoading() && sw1 != null && !sw1.isCancelled()) {
            state = State.CANCELED;
            sw1.cancel(true);
        }
    }

    @JsonIgnore
    public boolean isLoading() {
        return State.LOADING.equals(state);
    }

    public State getState() {
        return state;
    }

    public Exception getError() {
        return error;
    }

    void changeId(int i) {
        this.id = i;
    }

    public File asFile() {
        return new File(file);
    }

    enum State {
        NONE, ERROR, PENDING, LOADING, CANCELED, LOADED;
    }

    public interface LoadedListener {

        void onLoaded(PictLoader pictLoader);
    }

    public interface ProgressListener {

        void onProgress(PictLoader pictLoader, float percent);
    }

    public static class PictWorker extends SwingWorker<Void, Float> implements IIOReadProgressListener {

        private final PictLoader pictLoader;

        PictWorker(PictLoader pictLoader) {
            this.pictLoader = pictLoader;
        }

        @Override
        protected void process(List<Float> chunks) {
            float i = chunks.get(chunks.size() - 1);
            pictLoader.progress = i;
            pictLoader.fireProgress(i);
        }

        @Override
        protected Void doInBackground() {
            pictLoader.state = State.LOADING;
            try {
                pictLoader.image = PictUtils.getScaledImage(read(), pictLoader.w, pictLoader.h);
                pictLoader.state = State.LOADED;
            } catch (IOException e) {
                LOGGER.error("Error reading pict #" + pictLoader.id, e);
                pictLoader.state = State.ERROR;
                pictLoader.error = e;
            }
            return null;
        }

        @Override
        protected void done() {
            pictLoader.fireLoaded();
        }

        private Image read() throws IOException {
            final FileInputStream fin = new FileInputStream(pictLoader.file);
            final Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(pictLoader.getExt().replace(".", "").toUpperCase());
            final ImageReader imageReader = readers.next();
            final ImageInputStream iis = ImageIO.createImageInputStream(fin);
            imageReader.setInput(iis, false);
            imageReader.addIIOReadProgressListener(this);
            return imageReader.read(0);
        }

        public void imageComplete(ImageReader source) {
            publish(100f);
        }

        public void imageProgress(ImageReader source, float percentageDone) {
            publish(percentageDone);
        }

        public void imageStarted(ImageReader source, int imageIndex) {
            publish(0f);
        }

        public void readAborted(ImageReader source) {
            //not used
        }

        public void sequenceComplete(ImageReader source) {
            //not used
        }

        public void sequenceStarted(ImageReader source, int minIndex) {
            //not used
        }

        public void thumbnailComplete(ImageReader source) {
            //not used
        }

        public void thumbnailProgress(ImageReader source, float percentageDone) {
            //not used
        }

        public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
            //not used
        }

    }

}
