package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_CSV;
import static org.cmdbuild.servlets.json.ComunicationConstants.SEPARATOR;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.dataimport.CardFiller;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVCard;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImportCSV extends JSONBaseWithSpringContext {

	/**
	 * Stores in the session the records of the file that the user has uploaded
	 * 
	 * @param file
	 *            is the uploaded file
	 * @param separatorString
	 *            the separator of the csv file
	 * @param classId
	 *            the id of the class where the records will be stored
	 */
	@JSONExported
	public void uploadCSV(@Parameter(FILE_CSV) final FileItem file, //
			@Parameter(SEPARATOR) final String separatorString, //
			@Parameter("idClass") final Long classId) throws IOException, JSONException {
		clearSession();
		final DataAccessLogic dataAccessLogic = systemDataAccessLogic();
		final CSVData importedCsvData = dataAccessLogic.importCsvFileFor(file, classId, separatorString);
		sessionVars().setCsvData(importedCsvData);
	}

	/**
	 * 
	 * @return the serialization of the cards
	 */
	@JSONExported
	public JSONObject getCSVRecords() throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray rows = new JSONArray();
		out.put("rows", rows);
		final CSVData csvData = sessionVars().getCsvData();
		final DataAccessLogic dataAccessLogic = systemDataAccessLogic();
		out.put("headers", csvData.getHeaders());

		final CMClass entryType = dataAccessLogic.findClass(csvData.getImportedClassName());
		for (final CSVCard csvCard : csvData.getCards()) {
			rows.put(serializeCSVCard(csvCard, entryType));
		}

		return out;
	}

	@JSONExported
	// TODO: move to the logic to the logic??
	public void updateCSVRecords( //
			@Parameter("data") final JSONArray jsonCards //
	) throws JSONException {

		final DataAccessLogic dataAccessLogic = systemDataAccessLogic();
		final CSVData csvData = sessionVars().getCsvData();
		final CMClass importedClass = dataAccessLogic.findClass(csvData.getImportedClassName());
		final CardFiller cardFiller = new CardFiller(importedClass, userDataView(), lookupStore());

		for (int i = 0; i < jsonCards.length(); i++) {
			final JSONObject jsonCard = jsonCards.getJSONObject(i);
			final Long fakeId = jsonCard.getLong("Id");
			final CSVCard csvCard = csvData.getCard(fakeId);
			// ugly... it should not have knowledge of dao implementation
			final DBCard mutableCard = (DBCard) csvCard.getCMCard();
			for (final String attributeName : csvData.getHeaders()) {
				if (jsonCard.has(attributeName)) {
					final Object attributeValue = jsonCard.get(attributeName);
					if (csvCard.getInvalidAttributes().containsKey(attributeName)) {
						csvCard.getInvalidAttributes().remove(attributeName);
					}

					try {
						cardFiller.fillCardAttributeWithValue( //
								mutableCard, //
								attributeName, //
								attributeValue //
								);
					} catch (final Exception ex) {
						csvCard.addInvalidAttribute(attributeName, attributeValue);
					}
				}
			}
		}
	}

	@JSONExported
	public void storeCSVRecords() {
		final DataAccessLogic dataAccessLogic = systemDataAccessLogic();
		final CSVData csvData = sessionVars().getCsvData();

		final List<Long> createdCardFakeIdList = new LinkedList<Long>();

		for (final CSVCard csvCard : csvData.getCards()) {
			// Skip the cards with not well filled attributes
			if (csvCard.getInvalidAttributes().entrySet().isEmpty()) {
				final CMCard card = csvCard.getCMCard();
				final Card cardToCreate = Card.newInstance() //
						.withClassName(card.getType().getIdentifier().getLocalName()) //
						.withAllAttributes(card.getValues()) //
						.build();

				dataAccessLogic.createCard(cardToCreate, false);
				createdCardFakeIdList.add(csvCard.getFakeId());
			}
		}

		/*
		 * Remove the created cards. So if some cards have wrong fields them can
		 * be modified
		 */
		if (createdCardFakeIdList.size() > 0) {
			for (final Long id : createdCardFakeIdList) {
				csvData.removeCard(id);
			}
		}
	}

	private void clearSession() {
		sessionVars().setCsvData(null);
	}

	private JSONObject serializeCSVCard( //
			final CSVCard csvCard, //
			final CMClass entryType //

	) throws JSONException {

		final DataAccessLogic dataAccessLogic = systemDataAccessLogic();
		final CMCard cmCard = dataAccessLogic.resolveCardReferences(entryType, csvCard.getCMCard());
		final Card card = CardStorableConverter.of(cmCard).convert(cmCard);

		final JSONObject jsonCard = cardSerializer().toClient(card);
		addEmptyAttributesToSerialization(jsonCard, cmCard);
		final JSONObject output = new JSONObject();
		final JSONObject notValidValues = new JSONObject();
		jsonCard.put("Id", csvCard.getFakeId());
		jsonCard.put("IdClass_value", csvCard.getCMCard().getType().getName());
		output.put("card", jsonCard);

		for (final Entry<String, Object> entry : csvCard.getInvalidAttributes().entrySet()) {
			notValidValues.put(entry.getKey(), entry.getValue());
		}
		output.put("not_valid_values", notValidValues);

		return output;
	}

	/*
	 * The client needs to know all the attributes for each card, but the
	 * serializer does not add the attributes with no value to the JSONCard. Use
	 * this method to check the output of the serializer and add the empty
	 * attributes
	 */
	private void addEmptyAttributesToSerialization(final JSONObject jsonCard, final CMCard cmCard) throws JSONException {
		final CMClass entryType = cmCard.getType();
		for (final CMAttribute cmAttribute : entryType.getAttributes()) {
			final String attributeName = cmAttribute.getName();
			if (jsonCard.has(attributeName)) {
				continue;
			} else {
				jsonCard.put(attributeName, "");
			}
		}
	}

};
