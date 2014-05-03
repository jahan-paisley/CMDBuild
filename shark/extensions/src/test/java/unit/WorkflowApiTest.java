package unit;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SchemaApi.ClassInfo;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.cmdbuild.workflow.type.ReferenceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WorkflowApiTest {

	private static final String CLASS_NAME = "className";
	private static final int ID_CLASS = CLASS_NAME.hashCode();
	private static final int CARD_ID = 123;
	private static final String DESCRIPTION = "description";

	private FluentApiExecutor fluentApiExecutor;
	private SchemaApi schemaApi;
	private MailApi mailApi;

	private FluentApi fluentApi;
	private WorkflowApi workflowApi;

	@Before
	public void createWorkflowApi() throws Exception {
		fluentApiExecutor = mock(FluentApiExecutor.class);
		schemaApi = mock(SchemaApi.class);
		mailApi = mock(MailApi.class);

		fluentApi = new FluentApi(fluentApiExecutor);
		workflowApi = new WorkflowApi(fluentApiExecutor, schemaApi, mailApi);
	}

	@Test
	public void findClassByNameAndExistingCardWhenConvertingFromCardDescriptorToReferenceType() throws Exception {
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));
		when(fluentApiExecutor.fetch(any(ExistingCard.class))).thenReturn(
				cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));

		final CardDescriptor cardDescriptor = new CardDescriptor(CLASS_NAME, CARD_ID);
		final ReferenceType referenceType = workflowApi.referenceTypeFrom(cardDescriptor);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verify(fluentApiExecutor).fetch(any(ExistingCard.class));
		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByNameAndExistingCardWhenConvertingFromCardWithNoDescriptionToReferenceType() throws Exception {
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));
		when(fluentApiExecutor.fetch(any(ExistingCard.class))).thenReturn(
				cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));

		final Card card = cardWithNoDescription(CLASS_NAME, CARD_ID);
		final ReferenceType referenceType = workflowApi.referenceTypeFrom(card);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verify(fluentApiExecutor).fetch(any(ExistingCard.class));
		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByNameWhenConvertingFromCardWithDescriptionToReferenceType() throws Exception {
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));

		final Card card = cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION);
		final ReferenceType referenceType = workflowApi.referenceTypeFrom(card);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void fetchCardFromBaseClassWhenConvertingFromIdToReferenceType() throws Exception {
		when(fluentApiExecutor.fetch(any(ExistingCard.class))).thenReturn(
				cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));

		final ReferenceType referenceType = workflowApi.referenceTypeFrom(CARD_ID);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verify(fluentApiExecutor).fetch(any(ExistingCard.class));
		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByIdWhenConvertingFromReferenceTypeToCardDescriptor() throws Exception {
		when(schemaApi.findClass(ID_CLASS)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));

		final ReferenceType referenceType = new ReferenceType(CARD_ID, ID_CLASS, DESCRIPTION);
		final CardDescriptor cardDescriptor = workflowApi.cardDescriptorFrom(referenceType);

		assertThat(cardDescriptor.getId(), equalTo(CARD_ID));
		assertThat(cardDescriptor.getClassName(), equalTo(CLASS_NAME));

		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(ID_CLASS);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByIdAndExistingCardWhenConvertingFromReferenceTypeToCard() throws Exception {
		when(schemaApi.findClass(ID_CLASS)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));
		when(fluentApiExecutor.fetch(any(ExistingCard.class))).thenReturn(
				cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));

		final ReferenceType referenceType = new ReferenceType(CARD_ID, ID_CLASS, DESCRIPTION);
		final Card card = workflowApi.cardFrom(referenceType);

		assertThat(card.getId(), equalTo(CARD_ID));
		assertThat(card.getClassName(), equalTo(CLASS_NAME));
		assertThat(card.getDescription(), equalTo(DESCRIPTION));

		final ArgumentCaptor<ExistingCard> cardCaptor = ArgumentCaptor.forClass(ExistingCard.class);
		verify(fluentApiExecutor).fetch(cardCaptor.capture());

		final Card fetchedCard = cardCaptor.getValue();
		assertThat(fetchedCard.getClassName(), equalTo(CLASS_NAME));
		assertThat(fetchedCard.getId(), equalTo(CARD_ID));
		assertThat(fetchedCard.getAttributeNames().size(), equalTo(0));

		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(ID_CLASS);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	/*
	 * Utils
	 */

	private ClassInfo newClassInfo(final String className, final int idClass) {
		return new SchemaApi.ClassInfo(className, idClass);
	}

	private Card cardWithNoDescription(final String className, final int cardId) {
		return fluentApi.existingCard(className, cardId);
	}

	private Card cardWithDescription(final String className, final int cardId, final String description) {
		return fluentApi.existingCard(className, cardId).withDescription(description);
	}

}
