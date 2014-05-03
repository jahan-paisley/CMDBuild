package org.cmdbuild.api.fluent;

public class FluentApi {

	private final FluentApiExecutor executor;

	public FluentApi(final FluentApiExecutor executor) {
		this.executor = executor;
	}

	public FluentApiExecutor getExecutor() {
		return executor;
	}

	public NewCard newCard(final String className) {
		return new NewCard(this, className);
	}

	public ExistingCard existingCard(final CardDescriptor descriptor) {
		return new ExistingCard(this, descriptor.getClassName(), descriptor.getId());
	}

	public ExistingCard existingCard(final String className, final int id) {
		return new ExistingCard(this, className, id);
	}

	public NewRelation newRelation(final String domainName) {
		return new NewRelation(this, domainName);
	}

	public ExistingRelation existingRelation(final String domainName) {
		return new ExistingRelation(this, domainName);
	}

	public QueryClass queryClass(final String className) {
		return new QueryClass(this, className);
	}

	public FunctionCall callFunction(final String functionName) {
		return new FunctionCall(this, functionName);
	}

	public CreateReport createReport(final String title, final String format) {
		return new CreateReport(this, title, format);
	}

	public ActiveQueryRelations queryRelations(final CardDescriptor descriptor) {
		return new ActiveQueryRelations(this, descriptor.getClassName(), descriptor.getId());
	}

	public ActiveQueryRelations queryRelations(final String className, final int id) {
		return new ActiveQueryRelations(this, className, id);
	}

	public NewProcessInstance newProcessInstance(final String processClassName) {
		return new NewProcessInstance(this, processClassName);
	}

	public ExistingProcessInstance existingProcessInstance(final String processClassName, final int processId) {
		return new ExistingProcessInstance(this, processClassName, processId);
	}
	
	public QueryAllLookup queryLookup(final String type) {
		return new QueryAllLookup(this, type);
	}

}
