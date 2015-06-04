package org.utkuozdemir.watchdist.type;

public enum PasswordType {
	MASTER_PASSWORD("master.password", null),
	APP_PASSWORD("app.password", "old.and.new.app.passwords"),
	DB_RESET_PASSWORD("db.reset.password", "old.and.new.db.reset.passwords");

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
