package org.cmdbuild.workflow;

public enum ProcessAttributes {

	ProcessInstanceId("ProcessCode"), //
	FlowStatus("FlowStatus"), //
	ActivityInstanceId("ActivityInstanceId"), //
	CurrentActivityPerformers("NextExecutor"), //
	AllActivityPerformers("PrevExecutors"), //
	UniqueProcessDefinition("UniqueProcessDefinition"), //
	ActivityDefinitionId("ActivityDefinitionId"), //
	;

	private final String columnName;

	ProcessAttributes(final String columnName) {
		this.columnName = columnName;
	}

	@Override
	public String toString() {
		return columnName;
	}

	public String dbColumnName() {
		return columnName;
	}
}
