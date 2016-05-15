package org.utkuozdemir.watchdist.app;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.runtime.VersionInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.i18n.Language;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.type.PasswordType;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.FileManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.stage.Stage;

public class AppContext {
    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);
    private static volatile AppContext INSTANCE;
    private boolean newDbInitialized;
    private String initializedDbDirectory;

    private Stage mainStage;

    private AppContext() {
    }

    public static AppContext getInstance() {
        if (INSTANCE == null) {
            synchronized (DbManager.class) {
                //double checking Singleton instance
                if (INSTANCE == null) {
                    INSTANCE = new AppContext();
                }
            }
        }
        return INSTANCE;
    }

    private boolean isValidLanguage(String language) {
        try {
            Language l = Language.valueOf(language);
            return l != null;
        } catch (IllegalArgumentException e) {
            logger.info("Invalid language name: " + language);
            return false;
        }
    }

    public void launch(Stage mainStage) throws Exception {
        PlatformImpl.addListener(new PlatformImpl.FinishListener() {
            @Override
            public void idle(boolean implicitExit) {
            }

            @Override
            public void exitCalled() {
                DbManager.close();
            }
        });

        this.mainStage = mainStage;
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> takeErrorAction(e));
        logger.info("Using JAVAFX Version " + VersionInfo.getVersion());
        logger.info("Using JAVAFX Runtime Version " + VersionInfo.getRuntimeVersion());

        initializeDb();

        String language = DbManager.getProperty(Constants.KEY_LOCALE);
        if (language == null) {
            WindowManager.showLanguageSelectionWindow();
        } else {
            if (!isValidLanguage(language)) {
                DbManager.setProperty(Constants.KEY_LOCALE, null);
                WindowManager.showLanguageSelectionWindow();
            } else {
                Messages.setLocale(Language.valueOf(language).getLocale());
                initializePasswordPrompt();
            }
        }
    }

    private void initializePasswordPrompt() {
        String appPassword = DbManager.getProperty(PasswordType.APP_PASSWORD.getKey());
        String dbResetPassword = DbManager.getProperty(PasswordType.DB_RESET_PASSWORD.getKey());
        if (appPassword == null || appPassword.isEmpty() ||
                dbResetPassword == null || dbResetPassword.isEmpty()) {
            WindowManager.showSetPasswordsWindow();
        } else {
            WindowManager.showAppPasswordWindow();
        }
    }

    private void initializeDb() {
        try {
            Path dbFilePath = Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            logger.info("Using DB Path: " + dbFilePath.toString());

            Path dbDirectory = dbFilePath.getParent();
            logger.info("Using DB Directory: " + dbDirectory.toString());

            Path dbPath = FileManager.getDatabasePath();
            if (!Files.exists(dbPath)) {
                FileManager.resetDatabase();
                newDbInitialized = true;
                initializedDbDirectory = dbDirectory.toString();
            }
            DbManager.setDbPath(dbPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void takeErrorAction(Throwable e) {
        logger.error(e.getMessage(), e);
        WindowManager.showErrorAlert(Messages.get("error"), Messages.get("error.message"));
    }

    public boolean isNewDbInitialized() {
        return newDbInitialized;
    }

    public String getInitializedDbDirectory() {
        return initializedDbDirectory;
    }

    public Stage getMainStage() {
        return mainStage;
    }
}
