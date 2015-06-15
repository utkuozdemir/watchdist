package org.utkuozdemir.watchdist.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalDate;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "notes")
public class Notes {

	@DatabaseField(id = true, columnName = "date")
	private String date;

	@DatabaseField(columnName = "notes")
	private String notes;

	Notes() {
	}

	public Notes(LocalDate date, String notes) {
		this.date = date.toString();
		this.notes = notes;
	}

	public String getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date.toString();
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
