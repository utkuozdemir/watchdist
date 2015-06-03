package org.utkuozdemir.watchdist;

import com.google.common.base.Strings;
import com.j256.ormlite.logger.LocalLog;
import com.sun.javafx.runtime.VersionInfo;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.i18n.Language;
import org.utkuozdemir.watchdist.type.PasswordType;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.FileManager;
import org.utkuozdemir.watchdist.util.WindowManager;
import org.utkuozdemir.watchdist.i18n.Messages;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends Application {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private static boolean newDbInitialized = false;
	private static String initializedDbDirectory;

	public static void main(String[] args) {
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Thread.currentThread().setUncaughtExceptionHandler((t, e) -> takeErrorAction(e));
		logger.info("Using JAVAFX Version " + VersionInfo.getVersion());
		logger.info("Using JAVAFX Runtime Version " + VersionInfo.getRuntimeVersion());

		initializeDb();

		String language = DbManager.getProperty(Constants.LOCALE_KEY);
		if (language == null) {
			WindowManager.showLanguageSelectionWindow();
		} else {
			if (!isValidLanguage(language)) {
				DbManager.setProperty(Constants.LOCALE_KEY, null);
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
		if (Strings.isNullOrEmpty(appPassword) || Strings.isNullOrEmpty(dbResetPassword)) {
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
				App.initializedDbDirectory = dbDirectory.toString();
			}
			DbManager.initialize(dbPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void takeErrorAction(Throwable e) {
		logger.error(e.getMessage(), e);

		WindowManager.showErrorAlert(Messages.get("error"), Messages.get("error.message"));
	}

	public static boolean isNewDbInitialized() {
		return newDbInitialized;
	}

	public static String getInitializedDbDirectory() {
		return initializedDbDirectory;
	}

	private static boolean isValidLanguage(String language) {
		try {
			Language l = Language.valueOf(language);
			return l != null;
		} catch (IllegalArgumentException e) {
			logger.info("Invalid language name: " + language);
			return false;
		}
	}
}
