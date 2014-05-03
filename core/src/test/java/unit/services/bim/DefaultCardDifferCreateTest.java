package unit.services.bim;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.bim.connector.DefaultCardDiffer;
import org.cmdbuild.services.bim.connector.MapperRules;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultCardDifferCreateTest {

	private static final String CLASS_NAME = "theClass";
	private static final String CODE_ATTRIBUTE = "Code";
	private static final String THECODE = "CodeValue1";
	private DefaultCardDiffer differ;
	private final CMDataView dataView = mock(CMDataView.class);
	private final MapperRules mapperRules = mock(MapperRules.class);
	private final LookupLogic lookupLogic = mock(LookupLogic.class);

	@Before
	public void setUp() {
		this.differ = new DefaultCardDiffer(dataView, lookupLogic, mapperRules);
	}

	@Test
	public void ifSourceTypeIsNotACMClassDoNothing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		when(dataView.findClass(CLASS_NAME)).thenReturn(null);

		// when
		final CMCard card = differ.createCard(source);

		// then
		assertTrue(card == null);
		final InOrder inorder = inOrder(dataView, source);
		inorder.verify(source).getTypeName();
		inorder.verify(dataView).findClass(CLASS_NAME);
		verifyNoMoreInteractions(dataView, source);
		verifyZeroInteractions(mapperRules, lookupLogic);
	}

	@Test
	public void ifTargetClassHasNoAttributesDoNothing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		when(source.getTypeName()).thenReturn(CLASS_NAME);

		final CMClass theClass = mock(CMClass.class);
		when(dataView.findClass(CLASS_NAME)).thenReturn(theClass);

		final Iterable attributes = new ArrayList<CMAttribute>();
		when(theClass.getAttributes()).thenReturn(attributes);

		// when
		differ.createCard(source);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView, source);
		inorder.verify(source).getTypeName();
		inorder.verify(dataView).findClass(CLASS_NAME);
		inorder.verify(dataView).createCardFor(theClass);
		verifyNoMoreInteractions(dataView, source);
	}

	@Test
	public void ifSourceHasNoAttributesOfTargetClassDoNothing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(Attribute.NULL_ATTRIBUTE);

		final CMClass theClass = mock(CMClass.class);
		when(dataView.findClass(CLASS_NAME)).thenReturn(theClass);

		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(Attribute.NULL_ATTRIBUTE);

		final Iterable attributes = new ArrayList<CMAttribute>();
		final CMAttribute codeAttribute = mock(CMAttribute.class);
		((ArrayList<CMAttribute>) attributes).add(codeAttribute);
		when(theClass.getAttributes()).thenReturn(attributes);
		when(codeAttribute.getName()).thenReturn(CODE_ATTRIBUTE);

		// when
		final CMCard card = differ.createCard(source);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView, source);
		inorder.verify(source).getTypeName();
		inorder.verify(dataView).findClass(CLASS_NAME);
		inorder.verify(dataView).createCardFor(theClass);
		inorder.verify(source).getAttributeByName(CODE_ATTRIBUTE);
		verifyNoMoreInteractions(dataView, source);
		assertTrue(card == null);
	}

	@Test
	public void ifSourceHasOneAttributeOfTargetCreateTheCard() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		final Attribute sourceAttribute = mock(Attribute.class);
		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(sourceAttribute);
		when(sourceAttribute.isValid()).thenReturn(true);
		when(sourceAttribute.getValue()).thenReturn(THECODE);

		final CMClass theClass = mock(CMClass.class);
		when(dataView.findClass(CLASS_NAME)).thenReturn(theClass);

		final Iterable attributes = new ArrayList<CMAttribute>();
		final CMAttribute codeAttribute = mock(CMAttribute.class);
		((ArrayList<CMAttribute>) attributes).add(codeAttribute);
		when(theClass.getAttributes()).thenReturn(attributes);
		when(codeAttribute.getName()).thenReturn(CODE_ATTRIBUTE);

		final CMCard.CMCardDefinition cardDefinition = mock(CMCard.CMCardDefinition.class);
		when(dataView.createCardFor(theClass)).thenReturn(cardDefinition);

		final CMCard card = mock(CMCard.class);
		when(cardDefinition.save()).thenReturn(card);
		when(card.get(CODE_ATTRIBUTE)).thenReturn(THECODE);
		// when
		final CMCard card1 = differ.createCard(source);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView, source, cardDefinition);
		inorder.verify(source).getTypeName();
		inorder.verify(dataView).findClass(CLASS_NAME);
		inorder.verify(dataView).createCardFor(theClass);
		inorder.verify(source).getAttributeByName(CODE_ATTRIBUTE);
		inorder.verify(cardDefinition).set(CODE_ATTRIBUTE, THECODE);
		inorder.verify(cardDefinition).save();
		verifyNoMoreInteractions(dataView, source, cardDefinition);
		assertTrue(card1 != null);
		assertThat(card1.get(CODE_ATTRIBUTE).toString(), equalTo(THECODE));
	}
}
