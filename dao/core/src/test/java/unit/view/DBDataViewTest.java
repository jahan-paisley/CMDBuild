package unit.view;

import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

// TODO complete tests checking data translation
public class DBDataViewTest {

	private static final String CLASS_NAME = "className";
	private static final Long ID = 42L;

	private static final String NOT_ACTIVE_CLASS_NAME = "notActive";
	private static final Long NOT_ACTIVE_ID = 123L;

	private static final String DOMAIN_NAME = "domainName";

	private static final String FUNCTION_NAME = "functionName";

	private DBDriver driver;
	private DBDataView view;

	@Before
	public void setUp() throws Exception {
		driver = mock(DBDriver.class);
		view = new DBDataView(driver);
	}

	@Test
	public void classFoundById() throws Exception {
		when(driver.findClass(ID)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final DBClass dbClass = view.findClass(ID);

		assertThat(dbClass.getId(), equalTo(ID));

		verify(driver).findClass(ID);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void classFoundByName() throws Exception {
		when(driver.findClass(CLASS_NAME)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final DBClass dbClass = view.findClass(CLASS_NAME);

		assertThat(dbClass.getId(), equalTo(ID));
		assertThat(dbClass.getIdentifier().getLocalName(), equalTo(CLASS_NAME));

		verify(driver).findClass(CLASS_NAME);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void allClassesFound() throws Exception {
		when(driver.findAllClasses()) //
				.thenReturn(allClasses());

		final Iterable<DBClass> allClasses = view.findClasses();
		assertThat(sizeOf(allClasses), equalTo(2));
		assertThat(allClasses, hasItem(anActiveClass(CLASS_NAME, ID)));
		assertThat(allClasses, hasItem(aNotActiveClass(NOT_ACTIVE_CLASS_NAME, NOT_ACTIVE_ID)));

		verify(driver).findAllClasses();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void activeDomainsFound() throws Exception {
		when(driver.findAllDomains()).thenReturn(Lists.<DBDomain> newArrayList());

		view.findDomains();

		verify(driver).findAllDomains();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void allDomainsFound() throws Exception {
		view.findDomains();

		verify(driver).findAllDomains();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void findDomainsFor() throws Exception {
		final CMClass cmClass = mock(CMClass.class);
		when(driver.findAllDomains()).thenReturn(Lists.<DBDomain> newArrayList());

		view.findDomainsFor(cmClass);

		verify(driver).findAllDomains();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void domainFoundById() throws Exception {
		view.findDomain(ID);

		verify(driver).findDomain(ID);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void domainFoundByName() throws Exception {
		view.findDomain(DOMAIN_NAME);

		verify(driver).findDomain(DOMAIN_NAME);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void allFunctionsFound() throws Exception {
		view.findAllFunctions();

		verify(driver).findAllFunctions();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void functionFoundByName() throws Exception {
		view.findFunctionByName(FUNCTION_NAME);

		verify(driver).findFunction(FUNCTION_NAME);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void newCardCreatedButNotSaved() throws Exception {
		when(driver.findClass(any(Long.class))) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass mockClass = mock(CMClass.class);
		when(mockClass.getId()).thenReturn(ID);

		view.createCardFor(mockClass);

		verify(driver).findClass(ID);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void newCardCreatedAndSaved() throws Exception {
		when(driver.findClass(any(Long.class))) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass mockClass = mock(CMClass.class);
		when(mockClass.getId()).thenReturn(ID);

		view.createCardFor(mockClass).save();

		verify(driver).findClass(ID);
		verify(driver).create(any(DBEntry.class));
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void cardModifiedButNotSaved() throws Exception {
		// given

		when(driver.findClass(CLASS_NAME)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass clazz = mock(CMClass.class);
		final CMIdentifier mockIdentifier = mock(CMIdentifier.class);
		when(mockIdentifier.getLocalName()).thenReturn(CLASS_NAME);
		when(clazz.getIdentifier()).thenReturn(mockIdentifier);

		final CMCard card = mock(CMCard.class);
		when(card.getType()).thenReturn(clazz);
		when(card.getAllValues()).thenReturn(Maps.<String, Object> newHashMap().entrySet());

		// when
		view.update(card);

		// then
		verify(driver).findClass(CLASS_NAME, CMIdentifier.DEFAULT_NAMESPACE);
		verifyNoMoreInteractions(driver);

		verify(card).getType();
		verify(card).getId();
		verify(card).getAllValues();
		verifyNoMoreInteractions(card);
	}

	@Test
	public void cardModifiedAndSaved() throws Exception {
		// given
		when(driver.findClass(CLASS_NAME)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass clazz = mock(CMClass.class);
		final CMIdentifier mockIdentifier = mock(CMIdentifier.class);
		when(mockIdentifier.getLocalName()).thenReturn(CLASS_NAME);
		when(clazz.getIdentifier()).thenReturn(mockIdentifier);

		final CMCard card = mock(CMCard.class);
		when(card.getType()).thenReturn(clazz);
		when(card.getAllValues()).thenReturn(Maps.<String, Object> newHashMap().entrySet());

		// when
		view.update(card).save();

		// then
		verify(driver).findClass(CLASS_NAME, CMIdentifier.DEFAULT_NAMESPACE);
		verify(driver).update(any(DBEntry.class));
		verifyNoMoreInteractions(driver);

		verify(card).getType();
		verify(card).getId();
		verify(card).getAllValues();
		verifyNoMoreInteractions(card);
	}

	@Test
	public void queryExecuted() throws Exception {
		final QuerySpecs querySpecs = mock(QuerySpecs.class);

		view.executeQuery(querySpecs);

		verify(driver).query(querySpecs);
		verifyNoMoreInteractions(driver);
	}

	/*
	 * Utilities
	 */

	private List<DBClass> allClasses() {
		return Arrays.asList( //
				anActiveClass(CLASS_NAME, ID), //
				aNotActiveClass(NOT_ACTIVE_CLASS_NAME, NOT_ACTIVE_ID));
	}

	private DBClass anActiveClass(final String className, final Long id) {
		return aClass(className, id, true);
	}

	private DBClass aNotActiveClass(final String className, final Long id) {
		return aClass(className, id, false);
	}

	private DBClass aClass(final String className, final Long id, final boolean active) {
		final ClassMetadata classMetadata = new ClassMetadata();
		classMetadata.put(EntryTypeMetadata.ACTIVE, Boolean.valueOf(active).toString());
		return DBClass.newClass() //
				.withIdentifier(fromName(className)) //
				.withId(id) //
				.withAllMetadata(classMetadata) //
				.withAllAttributes(Collections.<DBAttribute> emptyList()) //
				.build();
	}

	private int sizeOf(final Iterable<?> iterable) {
		return Iterators.size(iterable.iterator());
	}

}
