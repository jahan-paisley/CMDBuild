package unit.api.fluent.ws;

import static org.cmdbuild.api.fluent.ExistingCard.attachment;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardExt;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExistingCardTest extends AbstractWsFluentApiTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Captor
	private ArgumentCaptor<List<Attribute>> attributeListCaptor;

	@Test
	public void parametersPassedToProxyWhenUpdatingExistingCard() throws Exception {
		// when
		api().existingCard(CLASS_NAME, CARD_ID) // \
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE) //
				.limitAttributes(ATTRIBUTE_3, ATTRIBUTE_4) //
				.update();

		// then
		verify(proxy()).updateCard(cardCapturer());
		verifyNoMoreInteractions(proxy());
		final Card wsCard = capturedCard();
		assertThat(wsCard.getClassName(), equalTo(CLASS_NAME));
		assertThat(wsCard.getId(), equalTo(CARD_ID));
		assertThat(wsCard.getAttributeList(), containsAttribute(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
	}

	@Test
	public void parametersPassedToProxyWhenDeletingExistingCard() throws Exception {
		// when
		api().existingCard(CLASS_NAME, CARD_ID) //
				.delete();

		// then
		verify(proxy()).deleteCard(CLASS_NAME, CARD_ID);
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void parametersPassedToProxyWhenFetchingExistingCard() throws Exception {
		// given
		final ExistingCard existingCard = api().existingCard(CLASS_NAME, CARD_ID) // \
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE) //
				.limitAttributes(ATTRIBUTE_3, ATTRIBUTE_4);
		when(proxy().getCardWithLongDateFormat( //
				eq(CLASS_NAME), //
				eq(CARD_ID), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardFor(existingCard));

		// when
		existingCard.fetch();

		// then
		verify(proxy()).getCardWithLongDateFormat( //
				eq(CLASS_NAME), //
				eq(existingCard.getId()), //
				attributeListCaptor.capture());
		final List<Attribute> attributes = attributeListCaptor.getValue();
		assertThat(attributes, containsAttribute(ATTRIBUTE_3));
		assertThat(attributes, containsAttribute(ATTRIBUTE_4));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void soapCardIsConvertedToFluentApiCardWhenFetchingExistingCard() throws Exception {
		// given
		final ExistingCard existingCard = api().existingCard(CLASS_NAME, CARD_ID) // \
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE) //
				.limitAttributes(ATTRIBUTE_3, ATTRIBUTE_4);
		when(proxy().getCardWithLongDateFormat( //
				eq(CLASS_NAME), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardFor(existingCard));

		// when
		final org.cmdbuild.api.fluent.Card card = existingCard.fetch();

		// then
		assertThat(card.getClassName(), equalTo(CLASS_NAME));
		assertThat(card.getId(), equalTo(CARD_ID));
		assertThat(card.getAttributes(), hasEntry(CODE_ATTRIBUTE, (Object) CODE_VALUE));
		assertThat(card.getAttributes(), hasEntry(DESCRIPTION_ATTRIBUTE, (Object) DESCRIPTION_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_1, (Object) ATTRIBUTE_1_VALUE));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_2, (Object) ATTRIBUTE_2_VALUE));
	}

	@Test
	public void referenceOrLookupAttributeValueIsReturnedAsStringRepresentationOfInteger() throws Exception {
		// FIXME test with mock type converter

		// given
		final ExistingCard existingCard = api().existingCard(CLASS_NAME, CARD_ID);
		when(proxy().getCardWithLongDateFormat( //
				eq(CLASS_NAME), //
				eq(existingCard.getId()), //
				anyListOf(Attribute.class))) //
				.thenReturn(soapCardWithReference(CLASS_NAME, CARD_ID, ATTRIBUTE_1, ANOTHER_CARD_ID));

		// when
		final org.cmdbuild.api.fluent.Card card = existingCard.fetch();

		// then
		assertThat(card.getClassName(), equalTo(CLASS_NAME));
		assertThat(card.getId(), equalTo(existingCard.getId()));
		assertThat(card.getAttributes(), hasEntry(ATTRIBUTE_1, (Object) Integer.toString(ANOTHER_CARD_ID)));
	}

	@Test
	public void attachmentsUploaded() throws Exception {
		// when
		api().existingCard(CLASS_NAME, CARD_ID) //
				.with(attachment(fileUrl(), "foo", "foo's category", "foo's description")) //
				.withAttachment(fileUrl(), "bar", "bar's category", "bar's description") //
				.update();

		// then
		verify(proxy()).uploadAttachment( //
				eq(CLASS_NAME), //
				eq(CARD_ID), //
				any(DataHandler.class), //
				eq("foo"), //
				eq("foo's category"), //
				eq("foo's description"));
		verify(proxy()).uploadAttachment( //
				eq(CLASS_NAME), //
				eq(CARD_ID), //
				any(DataHandler.class), //
				eq("bar"), //
				eq("bar's category"), //
				eq("bar's description"));
		verifyNoMoreInteractions(proxy());
	}

	/*
	 * Utils
	 */

	private CardExt soapCardFor(final org.cmdbuild.api.fluent.Card card) {
		final org.cmdbuild.services.soap.CardExt soapCard = new org.cmdbuild.services.soap.CardExt();
		soapCard.setClassName(card.getClassName());
		if (card.getId() != null) {
			soapCard.setId(card.getId());
		}
		soapCard.getAttributeList().addAll(attributesFor(card));
		return soapCard;
	}

	private List<Attribute> attributesFor(final org.cmdbuild.api.fluent.Card card) {
		final List<Attribute> attributes = new ArrayList<Attribute>();
		for (final Entry<String, Object> entry : card.getAttributes().entrySet()) {
			attributes.add(new Attribute() {
				{
					setName(entry.getKey());
					setValue(entry.getValue().toString());
				}
			});
		}
		return attributes;
	}

	private CardExt soapCardWithReference(final String className, final int cardId,
			final String referenceAttributeName, final int referenceId) {
		final CardExt card = new CardExt();
		card.setClassName(className);
		card.setId(CARD_ID);
		card.getAttributeList().add(new Attribute() {
			{
				setName(referenceAttributeName);
				setCode(Integer.toString(referenceId));
			}
		});
		return card;
	}

	private String fileUrl() throws IOException, MalformedURLException {
		return temporaryFolder.newFile().toURI().toURL().toString();
	}

}
