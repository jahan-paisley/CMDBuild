package unit.logic.mapping.json;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.query.clause.where.FunctionWhereClause;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.validation.Validator.ValidationError;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class JsonFilterMapperTest {

	private static final long USER_ID = 123L;
	private static final long ROLE_ID = 456L;

	private CMClass entryType;
	private CMDataView dataView;
	private OperationUser operationUser;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		entryType = mock(CMClass.class);

		final DBAttribute attr1 = new DBAttribute("attr1", new IntegerAttributeType(), null);
		final DBAttribute attr2 = new DBAttribute("attr2", new IntegerAttributeType(), null);

		when((Iterable<DBAttribute>) entryType.getActiveAttributes()).thenReturn(Lists.newArrayList(attr1, attr2));
		when(entryType.getAttribute(attr1.getName())).thenReturn(attr1);
		when(entryType.getAttribute(attr2.getName())).thenReturn(attr2);
		when(entryType.getName()).thenReturn("Clazz");

		dataView = mock(CMDataView.class);
		when(dataView.findClass(entryType.getName())).thenReturn(entryType);

		final AuthenticatedUser authUser = mock(AuthenticatedUser.class);
		when(authUser.getId()) //
				.thenReturn(USER_ID);
		final PrivilegeContext privilegeCtx = mock(PrivilegeContext.class);
		final CMGroup selectedGroup = mock(CMGroup.class);
		when(selectedGroup.getId()) //
				.thenReturn(ROLE_ID);
		operationUser = new OperationUser(authUser, privilegeCtx, selectedGroup);
	}

	@Test(expected = NullPointerException.class)
	public void nullFilterShouldThrowException() throws Exception {
		// given
		final FilterMapper filterMapper = jsonFilterMapper(null);

		// when
		filterMapper.whereClauses();
	}

	@Test(expected = ValidationError.class)
	public void malformedFilterShouldThrowException() throws Exception {
		// given
		final FilterMapper filterMapper = jsonFilterMapper(filter("{not_expected_key: value}"));

		// when
		filterMapper.whereClauses();
	}

	@Test
	public void shouldSuccessfullyDeserializeGlobalFilter() throws Exception {
		// given
		final FilterMapper filterMapper = jsonFilterMapper(filter("{attribute: {simple: {attribute: attr1, operator: greater, value: [5]}}, "
				+ "query: test}"));

		// when
		final Iterable<WhereClause> whereClauses = filterMapper.whereClauses();

		// then
		assertThat(size(whereClauses), equalTo(2));
		assertThat(get(whereClauses, 0), anyOf(instanceOf(OrWhereClause.class), instanceOf(SimpleWhereClause.class)));
		assertThat(get(whereClauses, 1), anyOf(instanceOf(OrWhereClause.class), instanceOf(SimpleWhereClause.class)));
	}

	@Test
	public void globalFilterContainingOnlyFullTextQueryMustReturnOrWhereClauseIfMoreThanOneAttribute() throws Exception {
		// given
		final String globalFilter = "{query: test}";
		final JSONObject globalFilterObject = new JSONObject(globalFilter);

		// when
		final FilterMapper filterMapper = jsonFilterMapper(globalFilterObject);
		final Iterable<WhereClause> whereClauses = filterMapper.whereClauses();

		// then
		assertThat(size(whereClauses), equalTo(1));
		assertThat(get(whereClauses, 0), instanceOf(OrWhereClause.class));
	}

	@Test
	public void joinElementsSuccessfullyRead() throws Exception {
		// given
		final FilterMapper filterMapper = jsonFilterMapper(filter("{relation: [{domain: foo, source: bar_1, destination: baz_1, type: any}, {domain: bar, source: bar_2, destination: baz_2, type: any}]}"));

		// when
		final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();

		// then
		assertThat(size(joinElements), equalTo(2));
	}

	@Test
	public void functionsUsedWithinFilter() throws Exception {
		// given
		final FilterMapper filterMapper = jsonFilterMapper(filter("{functions: [{name: foo}, {name: bar}]}"));

		// when
		final Iterable<WhereClause> whereClauses = filterMapper.whereClauses();

		// then
		assertThat(size(whereClauses), equalTo(2));

		final WhereClause first = get(whereClauses, 0);
		final WhereClause second = get(whereClauses, 1);

		assertThat(first, instanceOf(FunctionWhereClause.class));
		final FunctionWhereClause firstFunctionWhereClause = FunctionWhereClause.class.cast(first);
		assertThat(firstFunctionWhereClause.name, equalTo("foo"));
		assertThat(firstFunctionWhereClause.userId, equalTo(USER_ID));
		assertThat(firstFunctionWhereClause.roleId, equalTo(ROLE_ID));
		assertThat(firstFunctionWhereClause.entryType, equalTo(CMEntryType.class.cast(entryType)));

		assertThat(second, instanceOf(FunctionWhereClause.class));
		final FunctionWhereClause secondFunctionWhereClause = FunctionWhereClause.class.cast(second);
		assertThat(secondFunctionWhereClause.name, equalTo("bar"));
		assertThat(secondFunctionWhereClause.userId, equalTo(USER_ID));
		assertThat(secondFunctionWhereClause.roleId, equalTo(ROLE_ID));
		assertThat(secondFunctionWhereClause.entryType, equalTo(CMEntryType.class.cast(entryType)));
	}

	/*
	 * Utilities
	 */

	private JsonFilterMapper jsonFilterMapper(final JSONObject filterObject) {
		return JsonFilterMapper.newInstance() //
				.withDataView(dataView) //
				.withDataView(dataView) //
				.withEntryType(entryType) //
				.withFilterObject(filterObject) //
				.withOperationUser(operationUser) //
				.build();
	}

	private JSONObject filter(final String filter) throws Exception {
		return new JSONObject(filter);
	}

}
