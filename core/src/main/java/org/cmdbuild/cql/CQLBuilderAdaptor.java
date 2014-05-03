package org.cmdbuild.cql;

/**
 * Adaptor class for CQLBuilderListener interface. All method are empty.
 */
public class CQLBuilderAdaptor implements CQLBuilderListener {

	@Override
	public void addFromClass(final String className, final String classAs) {
	}

	@Override
	public void addFromClass(final int classId, final String classAs) {
	}

	@Override
	public void addGroupByElement(final String classDomainReference, final String attributeName) {
	}

	@Override
	public void addOrderByElement(final String classDomainReference, final String attributeName, final OrderByType type) {
	}

	@Override
	public void addSelectAttribute(final String attributeName, final String attributeAs,
			final String classNameOrReference) {
	}

	@Override
	public void defaultGroupBy() {
	}

	@Override
	public void defaultLimit() {
	}

	@Override
	public void defaultOffset() {
	}

	@Override
	public void defaultOrderBy() {
	}

	@Override
	public void defaultSelect() {
	}

	@Override
	public void defaultWhere() {
	}

	@Override
	public void endDomain() {
	}

	@Override
	public void endDomainMeta() {
	}

	@Override
	public void endDomainObjects() {
	}

	@Override
	public void endDomainRef() {
	}

	@Override
	public void endExpression() {
	}

	@Override
	public void endField() {
	}

	@Override
	public void endFrom() {
	}

	@Override
	public void endFromDomain() {
	}

	@Override
	public void endGroup() {
	}

	@Override
	public void endGroupBy() {
	}

	@Override
	public void endOrderBy() {
	}

	@Override
	public void endSelect() {
	}

	@Override
	public void endSelectFromClass() {
	}

	@Override
	public void endSelectFromDomain() {
	}

	@Override
	public void endSelectFromDomainMeta() {
	}

	@Override
	public void endSelectFromDomainObjects() {
	}

	@Override
	public void endSelectFunction() {
	}

	@Override
	public void endValue() {
	}

	@Override
	public void endWhere() {
	}

	@Override
	public void globalEnd() {
	}

	@Override
	public void globalStart() {
	}

	@Override
	public void selectAll() {
	}

	@Override
	public void setLimit(final int limit) {
	}

	@Override
	public void setLimit(final FieldInputValue limit) {
	}

	@Override
	public void setOffset(final int offset) {
	}

	@Override
	public void setOffset(final FieldInputValue offset) {
	}

	@Override
	public void startComplexField(final WhereType type, final boolean isNot, final String classOrDomainNameOrRef,
			final String[] fieldPath, final FieldOperator operator) {
	}

	@Override
	public void startDomain(final WhereType type, final String scopeReference, final String domainName,
			final DomainDirection direction, final boolean isNot) {
	}

	@Override
	public void startDomain(final WhereType type, final String scopeReference, final int domainId,
			final DomainDirection direction, final boolean isNot) {
	}

	@Override
	public void startDomainMeta() {
	}

	@Override
	public void startDomainObjects() {
	}

	@Override
	public void startDomainRef(final WhereType type, final String domainReference, final boolean isNot) {
	}

	@Override
	public void startExpression() {
	}

	@Override
	public void startFrom(final boolean history) {
	}

	@Override
	public void startFromDomain(final String scopeReference, final String domainName, final String domainAs,
			final DomainDirection direction) {
	}

	@Override
	public void startFromDomain(final String scopeReference, final int domainId, final String domainas,
			final DomainDirection direction) {
	}

	@Override
	public void startGroup(final WhereType type, final boolean isNot) {
	}

	@Override
	public void startGroupBy() {
	}

	@Override
	public void startLookupField(final WhereType type, final boolean isNot, final String classOrDomainNameOrRef,
			final String fieldId, final LookupOperator[] lookupPath, final FieldOperator operator) {
	}

	@Override
	public void startOrderBy() {
	}

	@Override
	public void startSelect() {
	}

	@Override
	public void startSelectFromClass(final String classNameOrReference) {
	}

	@Override
	public void startSelectFromDomain(final String domainNameOrReference) {
	}

	@Override
	public void startSelectFromDomainMeta() {
	}

	@Override
	public void startSelectFromDomainObjects() {
	}

	@Override
	public void startSelectFunction(final String functionName, final String functionAs) {
	}

	@Override
	public void startSimpleField(final WhereType type, final boolean isNot, final String classOrDomainNameOrRef,
			final String fieldId, final FieldOperator operator) {
	}

	@Override
	public void startValue(final FieldValueType type) {
	}

	@Override
	public void startWhere() {
	}

	@Override
	public void value(final Object o) {
	}

}
