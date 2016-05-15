package org.utkuozdemir.watchdist.app;

import com.j256.ormlite.logger.LocalLog;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        AppContext.getInstance().launch(stage);
    }
}