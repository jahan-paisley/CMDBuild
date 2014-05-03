package org.cmdbuild.servlets.json.management.export.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.json.management.export.CMDataSource;
import org.cmdbuild.servlets.json.management.export.DataExporter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class CsvExporter implements DataExporter {

	private final File csvFile;
	private final CsvPreference exportCsvPrefs;

	public CsvExporter(final File file, final CsvPreference preferences) {
		this.csvFile = file;
		this.exportCsvPrefs = preferences;
	}

	@Override
	public File export(final CMDataSource source) {
		try {
			return writeCsvDataToFile(source);
		} catch (final IOException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	private File writeCsvDataToFile(final CMDataSource source) throws IOException {
		final ICsvMapWriter writer = new CsvMapWriter(new FileWriter(csvFile), exportCsvPrefs);
		final Iterable<String> attributeNames = source.getHeaders();
		final String[] headers = Iterables.toArray(attributeNames, String.class);
		final Iterable<CMEntry> entriesToExport = source.getEntries();
		try {
			writer.writeHeader(headers);
			for (final CMEntry entry : entriesToExport) {
				try {
					final Map<String, Object> attributeNameToValue = createMapFrom(entry, attributeNames);
					writer.write(attributeNameToValue, headers);
				} catch (final RuntimeException e) {
					Log.PERSISTENCE.warn(String.format("Error exporting CSV for %s card %d", entry.getType()
							.getIdentifier().getLocalName(), entry.getId()));
					throw e;
				}
			}
		} finally {
			writer.close();
		}
		return csvFile;
	}

	private Map<String, Object> createMapFrom(final CMEntry entry, final Iterable<String> attributeNames) {
		final Map<String, Object> map = Maps.newHashMap();
		for (final String attributeName : attributeNames) {
			final Object value = (entry.get(attributeName) != null) ? entry.get(attributeName) : "";
			if (value instanceof IdAndDescription) {
				final String description = ((IdAndDescription) value).getDescription();
				map.put(attributeName, description != null ? description : "");
			} else {
				map.put(attributeName, value);
			}
		}

		return map;
	}

}
