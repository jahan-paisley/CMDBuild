package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.common.Constants.DATETIME_FOUR_DIGIT_YEAR_FORMAT;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.CARD_BINDING;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.EXPORT_MAPPING;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.IMPORT_MAPPING;
import static org.cmdbuild.servlets.json.ComunicationConstants.LAST_CHECKIN;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.logic.bim.project.ProjectLogic.Project;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Iterables;

public class ProjectSerializer {

	public static JSONArray toClient(final Iterable<Project> projects) throws JSONException {

		final JSONArray out = new JSONArray();
		for (final Project project : projects) {
			out.put(toClient(project));
		}
		return out;
	}

	public static JSONObject toClient(final Project project) throws JSONException {

		final JSONObject out = new JSONObject();

		// At the moment client supports only one binded card, therefore I
		// extract the first, if any.
		String bindedCard = StringUtils.EMPTY;
		if (!Iterables.isEmpty(project.getCardBinding())) {
			Iterator<String> it = project.getCardBinding().iterator();
			bindedCard = it.next();
		}

		out.put(ID, project.getProjectId());
		out.put(NAME, project.getName());
		out.put(DESCRIPTION, project.getDescription());
		out.put(ACTIVE, project.isActive());
		out.put(IMPORT_MAPPING, project.getImportMapping());
		out.put(EXPORT_MAPPING, project.getExportMapping());
		out.put(CARD_BINDING, bindedCard.isEmpty() ? "" : Long.parseLong(bindedCard));
		final DateTime lastCheckin = project.getLastCheckin();

		if (lastCheckin != null) {
			final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATETIME_FOUR_DIGIT_YEAR_FORMAT);
			out.put(LAST_CHECKIN, formatter.print(lastCheckin));
		}
		return out;
	}
}
