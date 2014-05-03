package learning;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.rmi.RemoteException;

import org.alfresco.webservice.dictionary.ClassPredicate;
import org.alfresco.webservice.dictionary.DictionaryFault;
import org.alfresco.webservice.dictionary.DictionaryServiceSoapPort;
import org.alfresco.webservice.types.AssociationDefinition;
import org.alfresco.webservice.types.ClassDefinition;
import org.alfresco.webservice.types.PropertyDefinition;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dms.DmsConfiguration;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.TestConfiguration;

public class TypeAndAspectDefinitionsTest {

	private static final String CMDBUILD_NAMESPACE_URI = "org.cmdbuild.dms.alfresco";
	private static final String CMDBUILD_XML_PREFIX = "cmdbuild";

	private static final String CMDBUILD_SUPER_TYPE = "SuperType";
	private static final String CMDBUILD_SUPER_ASPECT = "SuperAspect";

	private static DictionaryServiceSoapPort dictionaryService;

	private final DmsConfiguration configuration = new TestConfiguration();

	@Before
	public void createDictionaryService() throws Exception {
		final String address = configuration.getServerURL();
		WebServiceFactory.setEndpointAddress(address);
		dictionaryService = WebServiceFactory.getDictionaryService();
	}

	@Before
	public void startSession() throws Exception {
		AuthenticationUtils.startSession(configuration.getAlfrescoUser(), configuration.getAlfrescoPassword());
	}

	@After
	public void stopSession() throws Exception {
		AuthenticationUtils.endSession();
	}

	@Test
	public void gettingNothingReturnsNull() throws Exception {
		assertThat(classes(nothing(), nothing()), is(nullValue()));
	}

	@Test
	public void typesAndAspectsSuccessfullyReadedQueryingAllClasses() throws Exception {
		final ClassDefinition[] classDefinitions = classes(all(), //
				all());
		assertThat(classDefinitions, not(is(nullValue())));
		assertThat(classDefinitions, hasClassDefinitionWithName(CMDBUILD_SUPER_TYPE));
		assertThat(classDefinitions, hasClassDefinitionWithName("aType"));
		assertThat(classDefinitions, hasClassDefinitionWithName("anotherType"));
		assertThat(classDefinitions, hasClassDefinitionWithName("Document"));
		assertThat(classDefinitions, hasClassDefinitionWithName("Image"));
		assertThat(classDefinitions, hasClassDefinitionWithName(CMDBUILD_SUPER_ASPECT));
		assertThat(classDefinitions, hasClassDefinitionWithName("foo"));
		assertThat(classDefinitions, hasClassDefinitionWithName("bar"));
		assertThat(classDefinitions, hasClassDefinitionWithName("baz"));
		assertThat(classDefinitions, hasClassDefinitionWithName("summary"));
		assertThat(classDefinitions, hasClassDefinitionWithName("documentStatistics"));
		assertThat(classDefinitions, hasClassDefinitionWithName("displayable"));
	}

	@Test
	public void typesAndAspectsSuccessfullyReadedQueryingSubclasses() throws Exception {
		final ClassDefinition[] classDefinitions = classes( //
				subClassesOf(CMDBUILD_SUPER_TYPE), //
				subClassesOf(CMDBUILD_SUPER_ASPECT));
		assertThat(classDefinitions, not(is(nullValue())));
		assertThat(classDefinitions.length, equalTo(7));
		assertThat(classDefinitions, hasClassDefinitionWithName(CMDBUILD_SUPER_TYPE));
		assertThat(classDefinitions, hasClassDefinitionWithName("aType"));
		assertThat(classDefinitions, hasClassDefinitionWithName("anotherType"));
		assertThat(classDefinitions, not(hasClassDefinitionWithName("Document")));
		assertThat(classDefinitions, not(hasClassDefinitionWithName("Image")));
		assertThat(classDefinitions, hasClassDefinitionWithName(CMDBUILD_SUPER_ASPECT));
		assertThat(classDefinitions, hasClassDefinitionWithName("foo"));
		assertThat(classDefinitions, hasClassDefinitionWithName("bar"));
		assertThat(classDefinitions, hasClassDefinitionWithName("baz"));
		assertThat(classDefinitions, not(hasClassDefinitionWithName("summary")));
		assertThat(classDefinitions, not(hasClassDefinitionWithName("text")));
		assertThat(classDefinitions, not(hasClassDefinitionWithName("size")));
	}

	private Matcher<Object[]> hasClassDefinitionWithName(final String name) {
		return hasItemInArray(hasProperty("name", equalTo(nameWithUriFor(name))));
	}

	@Test
	public void dumpTypes() throws Exception {
		final ClassDefinition[] classDefinitions = classes( //
				subClassesOf(CMDBUILD_SUPER_TYPE), //
				nothing());
		for (final ClassDefinition classDefinition : classDefinitions) {
			dump(classDefinition);

		}
	}

	@Test
	public void dumpAspects() throws Exception {
		final ClassDefinition[] classDefinitions = classes(nothing(), subClassesOf(CMDBUILD_SUPER_ASPECT));
		for (final ClassDefinition classDefinition : classDefinitions) {
			dump(classDefinition);
		}
	}

	/*
	 * Utilities
	 */

	private ClassDefinition[] classes(final ClassPredicate types, final ClassPredicate aspects) throws RemoteException,
			DictionaryFault {
		return dictionaryService.getClasses(types, aspects);
	}

	private ClassPredicate nothing() {
		return new ClassPredicate(ArrayUtils.EMPTY_STRING_ARRAY, false, false);
	}

	private ClassPredicate all() {
		return null;
	}

	/**
	 * Builds the {@link ClassPredicate} for getting all subclasses of the
	 * specified element (type or aspect).
	 * 
	 * @param name
	 *            the name of the element (without prefix).
	 * 
	 * @return the {@link ClassPredicate} usable for getting all subclasses of
	 *         the specified element.
	 */
	private ClassPredicate subClassesOf(final String name) {
		return new ClassPredicate( //
				new String[] { nameWithPrefixFor(name) }, //
				true, //
				false);
	}

	/**
	 * Builds the element name completed with the expected prefix.
	 * 
	 * @param name
	 *            the name of the element (without prefix).
	 * 
	 * @return the full element name (e.g. "prefix:name").
	 */
	private String nameWithPrefixFor(final String name) {
		return format("%s:%s", CMDBUILD_XML_PREFIX, name);
	}

	/**
	 * Builds the name of an element as returned in
	 * {@link java.lang.instrument.ClassDefinition}.
	 * 
	 * @param name
	 *            the name of the element.
	 * 
	 * @return the full name of the element if the format "{uri}name".
	 */
	private String nameWithUriFor(final String name) {
		return format("{%s}%s", CMDBUILD_NAMESPACE_URI, name);
	}

	private void dump(final ClassDefinition classDefinition) throws DictionaryFault, RemoteException {
		System.out.println( //
				new ToStringBuilder(classDefinition) //
						.append("name", classDefinition.getName()) //
						.append("title", classDefinition.getTitle()) //
						.append("description", classDefinition.getDescription()) //
						.append("superclass", classDefinition.getSuperClass()) //
						.append("is aspect", classDefinition.isIsAspect()) //
				);

		final PropertyDefinition[] propertyDefinitions = classDefinition.getProperties();
		for (final PropertyDefinition propertyDefinition : propertyDefinitions == null ? new PropertyDefinition[] {}
				: propertyDefinitions) {
			System.out.println("\t" + //
					new ToStringBuilder(propertyDefinition) //
							.append("name", propertyDefinition.getName()) //
							.append("title", propertyDefinition.getTitle()) //
							.append("description", propertyDefinition.getDescription()) //
							.append("data type", propertyDefinition.getDataType()) //
							.append("default value", propertyDefinition.getDefaultValue()) //
							.append("is mandatory", propertyDefinition.isMandatory()) //
							.append("is read-only", propertyDefinition.isReadOnly()) //
			);
		}

		final AssociationDefinition[] associationDefinitions = classDefinition.getAssociations();
		for (final AssociationDefinition associationDefinition : associationDefinitions == null ? new AssociationDefinition[] {}
				: associationDefinitions) {
			System.out.println("\t" + //
					new ToStringBuilder(classDefinition) //
							.append("name", associationDefinition.getName()));
		}
	}

}
