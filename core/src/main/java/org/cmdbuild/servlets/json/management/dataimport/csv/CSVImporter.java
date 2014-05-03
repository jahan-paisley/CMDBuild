package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.servlets.json.management.dataimport.CardFiller;
import org.json.JSONException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CSVImporter {

	// a casual number from which start
	private static Long idCounter = 1000L;

	private final CsvPreference preferences;
	private final CMDataView view;
	private final CMClass importClass;
	private final LookupStore lookupStore;

	public CSVImporter( //
			final CMDataView view, //
			final LookupStore lookupStore, //
			final CMClass importClass, //
			final CsvPreference preferences //
		) {

		this.view = view;
		this.lookupStore = lookupStore;
		this.importClass = importClass;
		this.preferences = preferences;
	}

	public CSVData getCsvDataFrom(final FileItem csvFile) throws IOException, JSONException {
		return new CSVData(getHeaders(csvFile), getCsvCardsFrom(csvFile), importClass.getName());
	}

	private Map<Long, CSVCard> getCsvCardsFrom(final FileItem csvFile) throws IOException, JSONException {
		final Reader reader = new InputStreamReader(csvFile.getInputStream());
		final ICsvMapReader csvReader = new CsvMapReader(reader, preferences);
		return createCsvCards(csvReader);
	}

	private Map<Long, CSVCard> createCsvCards(final ICsvMapReader csvReader) throws IOException, JSONException {
		final Map<Long, CSVCard> csvCards = Maps.newHashMap();
		try {
			final String[] headers = csvReader.getCSVHeader(true);
			Map<String, String> currentLine = csvReader.read(headers);

			final CardFiller cardFiller = new CardFiller(importClass, view, lookupStore);
			while (currentLine != null) {
				final Long fakeId = getAndIncrementIdForCsvCard();
				final DBCard mutableCard = (DBCard) view.createCardFor(importClass);
				final CSVCard csvCard = new CSVCard(mutableCard, fakeId);
				for (final Entry<String, String> entry : currentLine.entrySet()) {
					try {
						cardFiller.fillCardAttributeWithValue( //
								mutableCard, //
								entry.getKey(), //
								entry.getValue() //
							);
					} catch (CardFiller.CardFillerException ex) {
						csvCard.addInvalidAttribute(ex.attributeName, ex.attributeValue);
					}
				}

				csvCards.put(fakeId, csvCard);
				currentLine = csvReader.read(headers);
			}
		} finally {
			csvReader.close();
		}

		return csvCards;
	}

	private static synchronized Long getAndIncrementIdForCsvCard() {
		return idCounter++;
	}

	private Iterable<String> getHeaders(final FileItem csvFile) throws IOException {
		final Reader reader = new InputStreamReader(csvFile.getInputStream());
		final ICsvMapReader csvReader = new CsvMapReader(reader, preferences);
		return Lists.newArrayList(csvReader.getCSVHeader(true));
	}
}