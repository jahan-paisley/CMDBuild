package unit.core.api.fluent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.Lookup;
import org.cmdbuild.api.fluent.QueryAllLookup;
import org.cmdbuild.api.fluent.QuerySingleLookup;
import org.cmdbuild.core.api.fluent.LogicFluentApiExecutor;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class FetchLookupTest {

	private static final String D1 = "d1";
	private static final String C1 = "c1";
	private FluentApiExecutor executor;
	private DataAccessLogic dataLogic;
	private LookupLogic lookupLogic;

	@Before
	public void setUp() throws Exception {
		dataLogic = mock(DataAccessLogic.class);
		lookupLogic = mock(LookupLogic.class);
		executor = new LogicFluentApiExecutor(dataLogic, lookupLogic);
	}

	@Test
	public void fetchAllLookupOfValue() throws Exception {
		// given
		org.cmdbuild.data.store.lookup.Lookup first = //
		org.cmdbuild.data.store.lookup.Lookup.newInstance()//
				.withId((long) 1)//
				.withCode(C1) //
				.withDescription(D1) //
				.build();
		org.cmdbuild.data.store.lookup.Lookup second = //
		org.cmdbuild.data.store.lookup.Lookup.newInstance()//
				.withId((long) 2)//
				.withCode("c2") //
				.withDescription("d2") //
				.build();
		Iterable<org.cmdbuild.data.store.lookup.Lookup> values = Arrays.asList(first, second);
		when(lookupLogic.getAllLookup(any(LookupType.class), any(Boolean.class))).thenReturn(values);
		QueryAllLookup queryLookup = mock(QueryAllLookup.class);
		when(queryLookup.getType()).thenReturn("thetype");
		
		// when
		Iterable<Lookup> elements = executor.fetch(queryLookup );

		// then
		verify(lookupLogic).getAllLookup(any(LookupType.class), any(Boolean.class));
		verifyZeroInteractions(dataLogic);
		Lookup _first = Iterables.get(elements, 0);
		Lookup _second = Iterables.get(elements, 1);
		assertThat(C1,equalTo(_first.getCode()));
		assertThat(D1,equalTo(_first.getDescription()));
		assertThat("c2",equalTo(_second.getCode()));
		assertThat("d2",equalTo(_second.getDescription()));
	}
	
	@Test
	public void fetchSingleLookupById() throws Exception {
		// given
		QuerySingleLookup querySingleLookup = mock(QuerySingleLookup.class);
		org.cmdbuild.data.store.lookup.Lookup value = org.cmdbuild.data.store.lookup.Lookup.newInstance()//
				.withId((long) 1)//
				.withCode(C1) //
				.withDescription(D1) //
				.build();
		when(lookupLogic.getLookup(any(Long.class))).thenReturn(value);
		
		//when
		Lookup result = executor.fetch(querySingleLookup);
		
		//then
		verify(lookupLogic).getLookup(any(Long.class));
		verifyZeroInteractions(dataLogic);
		assertThat(result.getCode(), equalTo(C1));
	}
}
