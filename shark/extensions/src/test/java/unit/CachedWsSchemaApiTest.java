package unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.MenuSchema;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.api.CachedWsSchemaApi;
import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SchemaApi.ClassInfo;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.cmdbuild.workflow.type.LookupType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that the web service implementation of {@link SharkWorkflowApiFactory}
 * calls the SOAP proxy correctly.
 */
public class CachedWsSchemaApiTest {

	private static final String CLASS1_NAME = "foo";
	private static final int CLASS1_ID = 101;
	private static final String CLASS2_NAME = "bar";
	private static final int CLASS2_ID = 102;
	private static final String PROCESS1_NAME = "baz";
	private static final int PROCESS1_ID = 103;

	private static final String LOOKUP_TYPE = "T1";

	private Private proxy;

	private SchemaApi api;

	@Before
	public void setUp() throws Exception {
		proxy = mock(Private.class);

		this.api = new CachedWsSchemaApi(proxy);
	}

	@Test
	public void classInformationsAreRetrievedAndCached() {
		ClassInfo classInfo;
		when(proxy.getCardMenuSchema()).thenReturn(wsMenuItem(CLASS1_NAME, CLASS1_ID, //
				wsMenuItem(CLASS2_NAME, CLASS2_ID) //
				));
		when(proxy.getMenuSchema()).thenReturn(wsMenuItem(CLASS1_NAME, CLASS1_ID, //
				wsMenuItem(CLASS2_NAME, CLASS2_ID) //
				));
		when(proxy.getActivityMenuSchema()).thenReturn(wsMenuItem(PROCESS1_NAME, PROCESS1_ID) //
				);

		classInfo = api.findClass(CLASS1_NAME);

		assertThat(classInfo.getId(), equalTo(CLASS1_ID));
		assertThat(classInfo.getName(), equalTo(CLASS1_NAME));
		assertThat(api.findClass(CLASS1_ID), is(sameInstance(classInfo)));

		classInfo = api.findClass(CLASS2_NAME);

		assertThat(classInfo.getId(), equalTo(CLASS2_ID));
		assertThat(classInfo.getName(), equalTo(CLASS2_NAME));

		classInfo = api.findClass(PROCESS1_NAME);

		assertThat(classInfo.getId(), equalTo(PROCESS1_ID));
		assertThat(classInfo.getName(), equalTo(PROCESS1_NAME));

		verify(proxy, times(1)).getMenuSchema();
		verify(proxy, times(1)).getCardMenuSchema();
		verify(proxy, times(1)).getActivityMenuSchema();
		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void classInformationsAreRetrievedOnCacheMiss() {
		when(proxy.getMenuSchema()).thenReturn(wsMenuItem(CLASS1_NAME, CLASS1_ID) //
				).thenReturn(wsMenuItem(CLASS2_NAME, CLASS2_ID) //
				);
		when(proxy.getCardMenuSchema()).thenReturn(wsMenuItem(CLASS1_NAME, CLASS1_ID) //
				).thenReturn(wsMenuItem(CLASS2_NAME, CLASS2_ID) //
				);
		when(proxy.getActivityMenuSchema()).thenReturn(wsMenuItem(PROCESS1_NAME, PROCESS1_ID) //
				);

		assertThat(api.findClass(CLASS1_NAME), not(nullValue()));

		verify(proxy, times(1)).getMenuSchema();
		verify(proxy, times(1)).getCardMenuSchema();
		verify(proxy, times(1)).getActivityMenuSchema();

		assertThat(api.findClass(CLASS2_ID), not(nullValue()));

		verify(proxy, times(2)).getMenuSchema();
		verify(proxy, times(2)).getCardMenuSchema();
		verify(proxy, times(2)).getActivityMenuSchema();

		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void lookupInformationsAreRetrievedByTypeAndCached() {
		when(proxy.getLookupList(LOOKUP_TYPE, null, false)).thenReturn(
				Arrays.asList(wsLookup(LOOKUP_TYPE, 1, "c1", "d1"), //
						wsLookup(LOOKUP_TYPE, 2, "c2", "d2") //
				));

		assertThat(api.selectLookupByCode(LOOKUP_TYPE, "c2"), is(lookup(LOOKUP_TYPE, 2, "c2", "d2")));

		verify(proxy, times(1)).getLookupList(LOOKUP_TYPE, null, false);

		assertThat(api.selectLookupByCode(LOOKUP_TYPE, "c1"), is(lookup(LOOKUP_TYPE, 1, "c1", "d1")));
		assertThat(api.selectLookupByDescription(LOOKUP_TYPE, "d2"), is(lookup(LOOKUP_TYPE, 2, "c2", "d2")));

		verify(proxy, times(1)).getLookupList(LOOKUP_TYPE, null, false);
		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void lookupInformationsByTypeAreRetrievedOnCacheMiss() {
		when(proxy.getLookupList(LOOKUP_TYPE, null, false)).thenReturn(
				Arrays.asList(wsLookup(LOOKUP_TYPE, 1, "c1", "d1") //
				)).thenReturn(Arrays.asList(wsLookup(LOOKUP_TYPE, 1, "c1", "d1"), //
				wsLookup(LOOKUP_TYPE, 2, "c2", "d2") //
				));

		assertThat(api.selectLookupByCode(LOOKUP_TYPE, "c1"), is(lookup(LOOKUP_TYPE, 1, "c1", "d1")));

		verify(proxy, times(1)).getLookupList(LOOKUP_TYPE, null, false);

		assertThat(api.selectLookupByCode(LOOKUP_TYPE, "c2"), is(lookup(LOOKUP_TYPE, 2, "c2", "d2")));

		verify(proxy, times(2)).getLookupList(LOOKUP_TYPE, null, false);
		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void lookupInformationsAreRetrievedByIdAndCached() {
		when(proxy.getLookupById(1)).thenReturn(wsLookup(LOOKUP_TYPE, 1, "c1", "d1"));
		when(proxy.getLookupById(2)).thenReturn(wsLookup(LOOKUP_TYPE, 2, "c2", "d2"));

		assertThat(api.selectLookupById(1), is(lookup(LOOKUP_TYPE, 1, "c1", "d1")));

		verify(proxy, times(1)).getLookupById(1);
		verify(proxy, never()).getLookupById(2);

		assertThat(api.selectLookupById(1), is(lookup(LOOKUP_TYPE, 1, "c1", "d1")));
		assertThat(api.selectLookupById(2), is(lookup(LOOKUP_TYPE, 2, "c2", "d2")));

		verify(proxy, times(1)).getLookupById(1);
		verify(proxy, times(1)).getLookupById(2);
		verifyNoMoreInteractions(proxy);
	}

	@Test
	public void lookupInformationsByIdAreCachedEvenByTypeQuery() {
		when(proxy.getLookupList(LOOKUP_TYPE, null, false)).thenReturn(
				Arrays.asList(wsLookup(LOOKUP_TYPE, 1, "c1", "d1"), //
						wsLookup(LOOKUP_TYPE, 2, "c2", "d2") //
				));
		when(proxy.getLookupById(3)).thenReturn(wsLookup("t2", 3, "c3", "d3"));

		api.selectLookupByCode(LOOKUP_TYPE, "c2");

		verify(proxy, times(1)).getLookupList(LOOKUP_TYPE, null, false);

		assertThat(api.selectLookupById(1), is(lookup(LOOKUP_TYPE, 1, "c1", "d1")));
		assertThat(api.selectLookupById(2), is(lookup(LOOKUP_TYPE, 2, "c2", "d2")));
		assertThat(api.selectLookupById(3), is(lookup("t2", 3, "c3", "d3")));

		verify(proxy, times(1)).getLookupById(3);
		verifyNoMoreInteractions(proxy);
	}

	/*
	 * Utils
	 */

	private MenuSchema wsMenuItem(final String name, final int id, final MenuSchema... children) {
		final MenuSchema menuItem = new MenuSchema();
		menuItem.setClassname(name);
		menuItem.setId(id);
		for (final MenuSchema child : children) {
			menuItem.getChildren().add(child);
		}
		return menuItem;
	}

	private Lookup wsLookup(final String type, final int id, final String code, final String description) {
		final Lookup lookup = new Lookup();
		lookup.setType(type);
		lookup.setId(id);
		lookup.setCode(code);
		lookup.setDescription(description);
		return lookup;
	}

	private LookupType lookup(final String type, final int id, final String code, final String description) {
		final LookupType lookup = new LookupType();
		lookup.setType(type);
		lookup.setId(id);
		lookup.setCode(code);
		lookup.setDescription(description);
		return lookup;
	}
}
