package unit.core.api.fluent;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.core.api.fluent.LogicFluentApiExecutor;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Maps;

public class UpdateExistingCardTest {

	private static final int ID = 111;
	private static final String DESCRIPTION_ATTRIBUTE_NAME = "Description";
	private static final String DESCR = "pippo";
	private static final String CLASS_NAME = "TheClass";
	private DataAccessLogic dataLogic;
	private FluentApiExecutor executor;
	private LookupLogic lookupLogic;

	@Before
	public void setUp() throws Exception {
		dataLogic = mock(DataAccessLogic.class);
		lookupLogic = mock(LookupLogic.class);
		executor = new LogicFluentApiExecutor(dataLogic, lookupLogic);
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateExistingCardWithoutClassnameThrowsException() throws Exception {
		// given
		final ExistingCard existingCard = mock(ExistingCard.class);

		// when
		executor.update(existingCard);

		// then
		verify(dataLogic).updateCard(any(org.cmdbuild.model.data.Card.class));
		verifyZeroInteractions(lookupLogic);
	}

	@Test
	public void updateDescriptionOfExistingCard() throws Exception {
		// given
		final ArgumentCaptor<org.cmdbuild.model.data.Card> cardToStoreCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.model.data.Card.class);

		final ExistingCard existingCard = mock(ExistingCard.class);
		when(existingCard.getClassName()).thenReturn(CLASS_NAME);
		when(existingCard.getId()).thenReturn(ID);

		final Map<String, Object> attributesMap = Maps.newHashMap();
		attributesMap.put(DESCRIPTION_ATTRIBUTE_NAME, DESCR);
		when(existingCard.getAttributes()).thenReturn(attributesMap);

		// when
		executor.update(existingCard);

		// then
		verify(dataLogic).updateCard(cardToStoreCaptor.capture());
		final org.cmdbuild.model.data.Card cardToStore = cardToStoreCaptor.getValue();
		assertTrue(cardToStore.getClassName().equals(CLASS_NAME));
		assertTrue(cardToStore.getId().intValue() == ID);
		assertTrue(cardToStore.getAttributes().get(DESCRIPTION_ATTRIBUTE_NAME).equals(DESCR));
		verifyZeroInteractions(lookupLogic);
	}

}
