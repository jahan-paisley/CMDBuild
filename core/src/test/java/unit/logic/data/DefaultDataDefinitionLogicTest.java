package unit.logic.data;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DefaultDataDefinitionLogicTest {

	private static final String CLASS_NAME = "foo";
	private static final Long CLASS_ID = 42L;
	private static final String ATTRIBUTE_NAME = "bar";

	private CMDataView dataView;
	private DataDefinitionLogic dataDefinitionLogic;

	@Before
	public void createDataDefinitionLogic() throws Exception {
		dataView = mock(CMDataView.class);
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dataView);
	}

	@Test
	public void createUnexistingClass() {
		// given
		final CMClass createdClass = mockClass(CLASS_NAME);
		final ArgumentCaptor<CMIdentifier> identifierCaptor = ArgumentCaptor.forClass(CMIdentifier.class);
		when(dataView.findClass(identifierCaptor.capture())) //
				.thenReturn(null, createdClass);
		when(dataView.create(any(CMClassDefinition.class))) //
				.thenReturn(createdClass);

		// when
		final CMClass returnedClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));

		// then
		assertThat(returnedClass.getName(), equalTo(createdClass.getName()));
		verify(dataView).findClass(any(CMIdentifier.class));
		assertThat(identifierCaptor.getValue().getLocalName(), equalTo(CLASS_NAME));
		assertThat(identifierCaptor.getValue().getNameSpace(), equalTo(null));
		verify(dataView).create(any(CMClassDefinition.class));
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void updateExistingClass() {
		// given
		final ArgumentCaptor<CMIdentifier> identifierCaptor = ArgumentCaptor.forClass(CMIdentifier.class);
		final CMClass existingClass = mockClass(CLASS_NAME);
		when(dataView.findClass(identifierCaptor.capture())) //
				.thenReturn(existingClass);

		// when
		dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));

		// then
		verify(dataView).findClass(any(CMIdentifier.class));
		assertThat(identifierCaptor.getValue().getLocalName(), equalTo(CLASS_NAME));
		assertThat(identifierCaptor.getValue().getNameSpace(), equalTo(null));
		verify(dataView).update(any(CMClassDefinition.class));
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void deletingUnexistingAttributeDoesNothing() throws Exception {
		// given
		final CMClass existingClass = mockClass(CLASS_NAME);
		when(existingClass.getName()) //
				.thenReturn(CLASS_NAME);
		final ArgumentCaptor<CMIdentifier> identifierCaptor = ArgumentCaptor.forClass(CMIdentifier.class);
		when(dataView.findClass(identifierCaptor.capture())) //
				.thenReturn(existingClass);

		// when
		dataDefinitionLogic.deleteOrDeactivate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(existingClass.getName())));

		// then
		verify(dataView).findClass(any(CMIdentifier.class));
		verify(existingClass).getAttribute(ATTRIBUTE_NAME);
		assertThat(identifierCaptor.getValue().getLocalName(), equalTo(CLASS_NAME));
		assertThat(identifierCaptor.getValue().getNameSpace(), equalTo(null));
	}

	/*
	 * Utilities
	 */

	private CMClass mockClass(final String name) {
		final CMClass mockClass = mock(CMClass.class);
		when(mockClass.getName()) //
				.thenReturn(name);
		return mockClass;
	}

	private ClassBuilder newClass(final String name) {
		return EntryType.newClass() //
				.withName(name);
	}

	private AttributeBuilder newAttribute(final String name) {
		return Attribute.newAttribute() //
				.withName(name);
	}

	private static EntryType a(final ClassBuilder classBuilder) {
		return classBuilder.build();
	}

	private static Attribute a(final AttributeBuilder attributeBuilder) {
		return attributeBuilder.build();
	}

}
