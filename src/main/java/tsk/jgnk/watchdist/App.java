package tsk.jgnk.watchdist;

import com.j256.ormlite.logger.LocalLog;
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
import tsk.jgnk.watchdist.util.WindowManager;

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

        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                takeErrorAction(e);
            }
        });

        WindowManager.showMainWindow(stage);
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
