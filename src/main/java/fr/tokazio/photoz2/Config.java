package fr.tokazio.photoz2;

public class Config {

    private static Config instance;
    private boolean debug = true;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public boolean debug() {
        return debug;
    }
}
