package org.cmdbuild.logic.validation.json;

import static org.cmdbuild.logic.mapping.json.Constants.DIRECTION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.PROPERTY_KEY;

import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.logic.validation.Validator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonSorterValidator implements Validator {

	private static final String MALFORMED_MSG = "Malformed sorters";
	private final JSONArray sorters;

	public JsonSorterValidator(final JSONArray sorters) {
		this.sorters = sorters;
	}

	@Override
	public void validate() throws ValidationError {
		try {
			for (int i = 0; i < sorters.length(); i++) {
				final JSONObject sorterObject = sorters.getJSONObject(i);
				if (!sorterObject.has(PROPERTY_KEY) || !sorterObject.has(DIRECTION_KEY)) {
					throw new IllegalArgumentException(MALFORMED_MSG);
				}
				validateDirectionValue(sorterObject);
			}
		} catch (final Exception ex) {
			throw new ValidationError(MALFORMED_MSG + ex.getMessage());
		}
	}

	private void validateDirectionValue(final JSONObject sorter) throws JSONException {
		final String direction = sorter.getString(DIRECTION_KEY);
		if (direction.equalsIgnoreCase(Direction.ASC.toString())
				|| direction.equalsIgnoreCase(Direction.DESC.toString())) {
			return;
		}
		throw new IllegalArgumentException("Direction value must be one of these values: ASC, DESC");
	}

}
