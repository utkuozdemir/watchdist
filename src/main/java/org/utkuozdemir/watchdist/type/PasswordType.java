package org.utkuozdemir.watchdist.type;

import org.utkuozdemir.watchdist.app.Constants;

public enum PasswordType {
	MASTER_PASSWORD(Constants.KEY_MASTER_PASSWORD, null),
	APP_PASSWORD(Constants.KEY_APP_PASSWORD, "old.and.new.app.passwords"),
	DB_RESET_PASSWORD(Constants.KEY_DB_RESET_PASSWORD, "old.and.new.db.reset.passwords");

	private final String key;
	private final String messageKey;

	PasswordType(String key, String messageKey) {
		this.key = key;
		this.messageKey = messageKey;
	}

	public String getKey() {
		return key;
	}

	public String getMessageKey() {
		return messageKey;
	}
}
