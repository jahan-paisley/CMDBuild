package unit.api.fluent.ws;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.junit.Test;

public class NewCardTest extends AbstractWsFluentApiTest {

	@Test
	public void createEmptyCard() throws Exception {
		api().newCard(CLASS_NAME) //
				.create();

		verify(proxy()).createCard(cardCapturer());
		verifyNoMoreInteractions(proxy());

		final Card wsCard = capturedCard();
		assertThat(wsCard.getClassName(), equalTo(CLASS_NAME));
		assertThat(wsCard.getAttributeList(), is(emptyCollectionOf(Attribute.class)));
	}

	@Test
	public void parametersPassedToProxyWhenNewCardCreated() throws Exception {
		api().newCard(CLASS_NAME) //
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE) //
				.create();

		verify(proxy()).createCard(cardCapturer());
		verifyNoMoreInteractions(proxy());

		final Card wsCard = capturedCard();
		assertThat(wsCard.getClassName(), equalTo(CLASS_NAME));
		assertThat(wsCard.getAttributeList(), containsAttribute(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
		assertThat(wsCard.getAttributeList(), not(containsAttribute(MISSING_ATTRIBUTE)));
	}

	@Test
	public void idIsValueReturnedFromProxyWhenNewCardCreated() throws Exception {
		final NewCard newCard = api().newCard(CLASS_NAME) //
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);

		when(proxy().createCard(any(Card.class))).thenReturn(CARD_ID);
		assertThat(newCard.create().getId(), is(CARD_ID));

		when(proxy().createCard(any(Card.class))).thenReturn(ANOTHER_CARD_ID);
		assertThat(newCard.create().getId(), is(ANOTHER_CARD_ID));
	}
}
