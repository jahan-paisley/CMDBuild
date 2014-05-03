package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.util.Map;

import org.cmdbuild.servlets.json.management.dataimport.csv.CSVCard;

public class CSVData {

	private final Iterable<String> headers;
	private final Map<Long, CSVCard> tempIdToCsvCard;
	private final String className;

	public CSVData( //
			final Iterable<String> headers, //
			final Map<Long, CSVCard> tempIdToCsvCard,
			final String className
		) {

		this.headers = headers;
		this.tempIdToCsvCard = tempIdToCsvCard;
		this.className = className;
	}

	public Iterable<String> getHeaders() {
		return headers;
	}

	public Iterable<CSVCard> getCards() {
		return tempIdToCsvCard.values();
	}

	public String getImportedClassName() {
		return className;
	}

	public CSVCard getCard(final Long id) {
		return tempIdToCsvCard.get(id);
	}

	public void removeCard(final Long id) {
		tempIdToCsvCard.remove(id);
	}

}
