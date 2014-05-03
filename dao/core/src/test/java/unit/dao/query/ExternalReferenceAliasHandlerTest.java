package unit.dao.query;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.ExternalReferenceAliasHandler;
import org.junit.Before;
import org.junit.Test;

public class ExternalReferenceAliasHandlerTest {

	private CMEntryType entryType;
	private CMAttribute attribute;

	@Before
	public void setUp() throws Exception {
		entryType = mock(CMEntryType.class);
		when(entryType.getName()).thenReturn("foo");

		attribute = mock(CMAttribute.class);
		when(attribute.getName()).thenReturn("bar");
	}

	@Test
	public void aliasForQueryFromEntryType() throws Exception {
		// when
		final ExternalReferenceAliasHandler handler = new ExternalReferenceAliasHandler(entryType, attribute);

		// then
		assertThat(handler.forQuery(), equalTo("foo#bar"));
		verify(entryType).getName();
		verifyNoMoreInteractions(entryType);
		verify(attribute).getName();
		verifyNoMoreInteractions(attribute);
	}

	@Test
	public void aliasForQueryFromEntryName() throws Exception {
		// when
		final ExternalReferenceAliasHandler handler = new ExternalReferenceAliasHandler("foo", attribute);

		// then
		assertThat(handler.forQuery(), equalTo("foo#bar"));
		verify(attribute).getName();
		verifyNoMoreInteractions(attribute);
	}

	@Test
	public void aliasForResultFromEntryType() throws Exception {
		// when
		final ExternalReferenceAliasHandler handler = new ExternalReferenceAliasHandler(entryType, attribute);

		// then
		assertThat(handler.forResult(), equalTo("foo#bar#Description"));
		verify(entryType).getName();
		verifyNoMoreInteractions(entryType);
		verify(attribute).getName();
		verifyNoMoreInteractions(attribute);
	}

	@Test
	public void aliasForResultFromEntryName() throws Exception {
		// when
		final ExternalReferenceAliasHandler handler = new ExternalReferenceAliasHandler("foo", attribute);

		// then
		assertThat(handler.forResult(), equalTo("foo#bar#Description"));
		verify(attribute).getName();
		verifyNoMoreInteractions(attribute);
	}

}
