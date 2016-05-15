package org.utkuozdemir.watchdist.util;

import org.utkuozdemir.watchdist.app.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
    public static Path getDatabasePath() {
        try {
            Path filePath = Paths.get(FileManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path directory = filePath.getParent();
            String url = directory.toString() + File.separator + Constants.DB_NAME;
            return Paths.get(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getLogFilePath() {
        try {
            return Paths.get(
                    System.getProperty("user.home") +
                            File.separator +
                            "logs" +
                            File.separator +
                            Constants.LOG_FILE_NAME
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getCleanExcelTemplateInputStream() {
        return FileManager.class.getClassLoader().getResourceAsStream("template.xls");
    }

    private static InputStream getCleanDatabaseInputStream() {
        return FileManager.class.getClassLoader().getResourceAsStream("clean_db.db");
    }

    public static void resetDatabase() {
        try {
            Path databasePath = getDatabasePath();
            Files.deleteIfExists(databasePath);
            Files.copy(getCleanDatabaseInputStream(), databasePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
