package org.cmdbuild.model.profile;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class UIConfigurationObjectMapper extends ObjectMapper {

	public UIConfigurationObjectMapper() {
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
