package org.cmdbuild.services.bim.connector.export;

import java.util.Map;

import org.cmdbuild.bim.model.Entity;

import com.google.common.collect.MapDifference.ValueDifference;

public interface GenericMapper {

	void setConfiguration(Object input);

	void setTarget(Object input, Output output);

	Map<String, Entity> getSourceData();

	Map<String, Entity> getTargetData();

	String getLastGeneratedOutput(Object input);

	void beforeExecution();

	void afterExecution(Output output);

	void executeSynchronization(Map<String, Entity> entriesToCreate,
			Map<String, ValueDifference<Entity>> entriesToUpdate, Map<String, Entity> entriesToRemove, Output output);

}
