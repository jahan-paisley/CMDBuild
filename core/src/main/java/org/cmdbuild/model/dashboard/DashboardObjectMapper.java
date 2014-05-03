package org.cmdbuild.model.dashboard;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class DashboardObjectMapper extends ObjectMapper {

	public DashboardObjectMapper() {
		super();
		setSerializationConfig(copySerializationConfig().withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL) // to
																														// exclude
																														// null
																														// values
				.withSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY) // to
																				// exclude
																				// empty
																				// map
																				// or
																				// array
		);
	}
}
