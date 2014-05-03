package unit.logic.mapping.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.mapping.json.JsonAttributeFilterBuilder;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonFilterBuilderTest {

	private CMClass entryType;
	private CMDataView dataView;

	@Before
	public void setUpMocks() {
		entryType = mock(CMClass.class);

		final CMAttribute mockCodeAttribute = createMockForAttribute("Code", new TextAttributeType());
		final CMAttribute mockDescriptionAttribute = createMockForAttribute("Description", new TextAttributeType());
		final CMAttribute mockAgeAttribute = createMockForAttribute("Age", new IntegerAttributeType());

		when(entryType.getName()).thenReturn("Clazz");
		when(entryType.getAttribute("Code")).thenReturn(mockCodeAttribute);
		when(entryType.getAttribute("Description")).thenReturn(mockDescriptionAttribute);
		when(entryType.getAttribute("Age")).thenReturn(mockAgeAttribute);

		dataView = mock(CMDataView.class);
		when(dataView.findClass(entryType.getName())).thenReturn(entryType);
	}

	private CMAttribute createMockForAttribute(final String name, final CMAttributeType type) {
		final CMAttribute mockAttribute = mock(CMAttribute.class);
		when(mockAttribute.getName()).thenReturn(name);
		when((CMAttributeType) mockAttribute.getType()).thenReturn(type);
		return mockAttribute;
	}

	@Test(expected = IllegalArgumentException.class)
	public void notExpectedKeyShouldThrowException() throws Exception {
		// given
		final String filter = "{not_expected_key: {attribute: Code, operator: contain, value: [od]}}";

		// when
		whereClauseFrom(filter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notExcpectedOperatorShouldThrowException() throws Exception {
		// given
		final String filter = "{simple: {attribute: Code, operator: fake_operator, value: [od]}}";

		// when
		whereClauseFrom(filter);
	}

	@Test
	public void shouldSuccessfullyCreateSimpleWhereClause() throws Exception {
		// given
		final String filter = "{simple: {attribute: Code, operator: contain, value: [od]}}";

		// when
		final WhereClause wc = whereClauseFrom(filter);

		// then
		assertTrue(wc instanceof SimpleWhereClause);
	}

	@Test(expected = IllegalArgumentException.class)
	public void andClauseWithOnlyOneConditionShouldFail() throws Exception {
		// given
		final String filter = "{and: [{simple: {attribute: Code, operator: contain, value: [od]}}]}";

		// when
		whereClauseFrom(filter);
	}

	@Test
	public void shouldSuccessfullyCreateAndWhereClause() throws Exception {
		// given
		final String filter = "{and: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";

		// when
		final WhereClause wc = whereClauseFrom(filter);

		// then
		assertTrue(wc instanceof AndWhereClause);
	}

	@Test(expected = IllegalArgumentException.class)
	public void orClauseWithOnlyOneConditionShouldFail() throws Exception {
		// given
		final String filter = "{or: [{simple: {attribute: Code, operator: contain, value: [od]}}]}";

		// when
		whereClauseFrom(filter);
	}

	@Test
	public void shouldSuccessfullyCreateOrWhereClause() throws Exception {
		// given
		final String filter = "{or: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";

		// when
		final WhereClause wc = whereClauseFrom(filter);

		// then
		assertTrue(wc instanceof OrWhereClause);
	}

	@Test
	public void shouldSuccessfullyCreateAndWhereClauseWithMoreThanTwoConditions() throws Exception {
		// given
		final String filter = "{and: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Age, operator: greater, value: [5]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";

		// when
		final WhereClause wc = whereClauseFrom(filter);

		// then
		assertTrue(wc instanceof AndWhereClause);
		final AndWhereClause awc = (AndWhereClause) wc;
		assertEquals(awc.getClauses().size(), 3);
	}

	@Test
	public void shouldSuccessfullyCreateOrWhereClauseWithMoreThanTwoConditions() throws Exception {
		// given
		final String filter = "{or: [{simple: {attribute: Code, operator: contain, value: [od]}}, "
				+ "{simple: {attribute: Age, operator: greater, value: [5]}}, "
				+ "{simple: {attribute: Description, operator: like, value: [DEsc]}}]}";

		// when
		final WhereClause wc = whereClauseFrom(filter);

		// then
		assertTrue(wc instanceof OrWhereClause);
		final OrWhereClause owc = (OrWhereClause) wc;
		assertEquals(owc.getClauses().size(), 3);
	}

	private WhereClause whereClauseFrom(final String filter) throws Exception {
		return new JsonAttributeFilterBuilder(new JSONObject(filter), entryType, dataView).build();
	}

}
