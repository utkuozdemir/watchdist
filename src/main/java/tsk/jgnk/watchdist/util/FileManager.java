package tsk.jgnk.watchdist.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
    public static Path getDatabaseTemplate() {
        try {
            Path filePath = Paths.get(FileManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path directory = filePath.getParent();
            String url = directory.toString() + File.separator + Constants.DB_NAME;
            return Paths.get(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getExcelTemplate() {
        try {
            Path filePath = Paths.get(FileManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path directory = filePath.getParent();
            String url = directory.toString() + File.separator + Constants.TEMPLATE_NAME;
            return Paths.get(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
