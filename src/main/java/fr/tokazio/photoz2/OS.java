package fr.tokazio.photoz2;

public class OS {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    private OS() {
        super();
    }

    public static boolean isWindows() {
        return (OS_NAME.contains("win"));
    }

    public static boolean isMac() {
        return (OS_NAME.contains("mac"));
    }

    public static boolean isUnix() {
        return OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix");
    }

    public static boolean isSolaris() {
        return (OS_NAME.contains("sunos"));
    }

}