package unit.api.fluent.ws;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;
import static utils.matchers.QueryEqualFilterMatcher.containsFilter;

import java.util.List;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.QueryClass;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryClassTest extends AbstractWsFluentApiTest {

	@Captor
	private ArgumentCaptor<List<Attribute>> attributeListCaptor;

	private QueryClass queryClass;

	@Before
	public void createQueryClass() throws Exception {
		queryClass = api().queryClass(CLASS_NAME) //
				.withCode(CODE_VALUE) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE) //
				.limitAttributes(CODE_ATTRIBUTE, DESCRIPTION_ATTRIBUTE, ATTRIBUTE_1, ATTRIBUTE_2);
	}

	@Test
	public void parametersPassedToProxyWhenFetchingClass() throws Exception {
		when(proxy().getCardListWithLongDateFormat( //
				eq(queryClass.getClassName()), //
				anyListOf(Attribute.class), //
				any(Query.class), //
				anyListOf(Order.class), //
				anyInt(), //
				anyInt(), //
				anyString(), //
				any(CqlQuery.class)) //
		).thenReturn(cardList(queryClass.getClassName(), CARD_ID, ANOTHER_CARD_ID));

		queryClass.fetch();

		verify(proxy()).getCardListWithLongDateFormat( //
				eq(queryClass.getClassName()), //
				attributeListCaptor.capture(), //
				queryCapturer(), //
				anyListOf(Order.class), //
				eq(0), //
				eq(0), //
				isNull(String.class), //
				isNull(CqlQuery.class));
		verifyNoMoreInteractions(proxy());

		final List<Attribute> attributes = attributeListCaptor.getValue();
		assertThat(attributes, containsAttribute(CODE_ATTRIBUTE));
		assertThat(attributes, containsAttribute(DESCRIPTION_ATTRIBUTE));
		assertThat(attributes, containsAttribute(ATTRIBUTE_1));
		assertThat(attributes, containsAttribute(ATTRIBUTE_2));

		final Query query = capturedQuery();
		assertThat(query.getFilter(), equalTo(null));
		assertThat(query.getFilterOperator(), not(equalTo(null)));

		final FilterOperator filterOperator = query.getFilterOperator();
		assertThat(filterOperator.getOperator(), equalTo("AND"));

		final List<Query> queries = filterOperator.getSubquery();
		assertThat(queries.size(), equalTo(4));
		assertThat(queries, containsFilter(CODE_ATTRIBUTE, CODE_VALUE));
		assertThat(queries, containsFilter(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(queries, containsFilter(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(queries, containsFilter(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
	}

	@Test
	public void soapCardIsConvertedToFluentApiCardWhenFetchingClass() throws Exception {
		when(proxy().getCardListWithLongDateFormat( //
				eq(queryClass.getClassName()), //
				anyListOf(Attribute.class), //
				any(Query.class), //
				anyListOf(Order.class), //
				anyInt(), //
				anyInt(), //
				anyString(), //
				any(CqlQuery.class)) //
		).thenReturn(cardList(queryClass.getClassName(), CARD_ID, ANOTHER_CARD_ID));

		final List<Card> descriptors = queryClass.fetch();
		assertThat(descriptors.size(), equalTo(2));
		assertThat(descriptors.get(0).getId(), equalTo(CARD_ID));
		assertThat(descriptors.get(1).getId(), equalTo(ANOTHER_CARD_ID));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void descriptorsListObtainedWhenFetchingClassIsUnmodifiable() throws Exception {
		when(proxy().getCardListWithLongDateFormat( //
				eq(queryClass.getClassName()), //
				anyListOf(Attribute.class), //
				any(Query.class), //
				anyListOf(Order.class), //
				anyInt(), //
				anyInt(), //
				anyString(), //
				any(CqlQuery.class)) //
		).thenReturn(cardList(queryClass.getClassName(), CARD_ID, ANOTHER_CARD_ID));

		final List<Card> cards = queryClass.fetch();
		cards.clear();
	}

	private CardList cardList(final String className, final int... ids) {
		final CardList cardList = new CardList();
		cardList.setTotalRows(ids.length);
		final List<org.cmdbuild.services.soap.Card> cards = cardList.getCards();
		for (final int id : ids) {
			final org.cmdbuild.services.soap.Card soapCard = new org.cmdbuild.services.soap.Card();
			soapCard.setClassName(className);
			soapCard.setId(id);
			cards.add(soapCard);
		}
		return cardList;
	}

}
