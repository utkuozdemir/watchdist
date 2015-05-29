package tsk.jgnk.watchdist;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import com.sun.javafx.runtime.VersionInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thehecklers.monologfx.MonologFX;
import org.thehecklers.monologfx.MonologFXBuilder;
import org.thehecklers.monologfx.MonologFXButton;
import org.thehecklers.monologfx.MonologFXButtonBuilder;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

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

            String dbUrl = dbDirectory.toString() + File.separator + Constants.DB_NAME;

            InputStream dbInputStream = DbManager.class.getClassLoader().getResourceAsStream("clean_db.db");
//            URL dbResource = (URL) resourceAsStream;
            checkNotNull(dbInputStream);
            Path dbPath = Paths.get(dbUrl);
            if (!Files.exists(dbPath)) {
                Files.copy(dbInputStream, dbPath);
                newDbCreated = true;
            }

            ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbUrl);
            DbManager.initialize(connectionSource);

            Path templateFilePath = Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path templateDirectory = templateFilePath.getParent();
            String templateUrl = templateDirectory.toString() + File.separator + Constants.TEMPLATE_NAME;

            InputStream templateInputStream = DbManager.class.getClassLoader().getResourceAsStream("template.xls");
            checkNotNull(templateInputStream);
            Path templatePath = Paths.get(templateUrl);
            if (!Files.exists(templatePath)) {
                Files.copy(templateInputStream, templatePath);
                newTemplateCreated = true;
            }

            WindowManager.showInitializationInfo(
                    newDbCreated ? dbDirectory.toString() : null,
                    newTemplateCreated ? templateDirectory.toString() : null
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void takeErrorAction(Throwable e) {
        logger.error(e.getMessage(), e);
        MonologFXButton continueButton = MonologFXButtonBuilder.create()
                .label(Messages.get("app.continue"))
                .defaultButton(true)
                .type(MonologFXButton.Type.IGNORE).build();
        MonologFXButton exitButton = MonologFXButtonBuilder.create()
                .label(Messages.get("app.exit"))
                .type(MonologFXButton.Type.ABORT)
                .build();

        MonologFX mono = MonologFXBuilder.create()
                .modal(true)
                .titleText(Messages.get("error"))
                .message(Messages.get("error.message") +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        e.getMessage())
                .type(MonologFX.Type.ERROR)
                .button(continueButton)
                .button(exitButton)
                .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                .build();
        MonologFXButton.Type choice = mono.show();
        if (choice == MonologFXButton.Type.ABORT) {
            Platform.exit();
        }
    }
}
