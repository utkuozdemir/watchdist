package tsk.jgnk.watchdist;

import com.j256.ormlite.logger.LocalLog;
import com.sun.javafx.runtime.VersionInfo;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.FileManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Using JAVAFX Version " + VersionInfo.getVersion());
        logger.info("Using JAVAFX Runtime Version " + VersionInfo.getRuntimeVersion());
        initialize();

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> takeErrorAction(e));

        WindowManager.showMainWindow(stage);
    }

    private void initialize() {
        try {
            boolean newDbCreated = false;
            boolean newTemplateCreated = false;

            Path dbFilePath = Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            logger.info("Using DB Path: " + dbFilePath.toString());

            Path dbDirectory = dbFilePath.getParent();
            logger.info("Using DB Directory: " + dbDirectory.toString());

			Path dbPath = FileManager.getDatabasePath();
			if (!Files.exists(dbPath)) {
				FileManager.resetDatabase();
				newDbCreated = true;
			}
			DbManager.initialize(dbPath);

			Path templatePath = FileManager.getExcelTemplatePath();
			if (!Files.exists(templatePath)) {
				FileManager.resetExcelTemplate();
				newTemplateCreated = true;
			}

            WindowManager.showInitializationInfo(
					newDbCreated ? dbDirectory.toString() : null,
					newTemplateCreated ? templatePath.getParent().toString() : null
			);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void takeErrorAction(Throwable e) {
        logger.error(e.getMessage(), e);

		WindowManager.showErrorAlert(Messages.get("error"), Messages.get("error.message"));
	}
}
