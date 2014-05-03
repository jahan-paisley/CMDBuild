package unit.services.bim;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.bim.connector.DefaultCardDiffer;
import org.cmdbuild.services.bim.connector.MapperRules;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultCardDifferUpdateTest {

	private static final String CLASS_NAME = "theClass";
	private static final String OTHER_CLASS = "otherClass";
	private static final String CODE_ATTRIBUTE = "Code";
	private static final String THECODE = "CodeValue1";
	private static final Object OTHER_CODE = "CodeValue2";
	private DefaultCardDiffer differ;
	private final CMDataView dataView = mock(CMDataView.class);
	private final MapperRules mapperRules = mock(MapperRules.class);
	private final LookupLogic lookupLogic = mock(LookupLogic.class);

	@Before
	public void setUp() {
		this.differ = new DefaultCardDiffer(dataView, lookupLogic, mapperRules);
	}

	@Test
	public void ifSourceAndTargetAreOfDifferentClassesDoNothing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		final CMCard target = mock(CMCard.class);
		final CMClass theClass = mock(CMClass.class);
		when(target.getType()).thenReturn(theClass);
		when(theClass.getName()).thenReturn(CLASS_NAME);
		when(source.getTypeName()).thenReturn(OTHER_CLASS);

		// when
		differ.updateCard(source, target);

		// then
		verifyZeroInteractions(dataView, mapperRules, lookupLogic);
	}

	@Test
	public void ifTargetClassHasNoAttributesDoNothing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		final CMCard target = mock(CMCard.class);
		final CMClass theClass = mock(CMClass.class);
		when(target.getType()).thenReturn(theClass);
		when(theClass.getName()).thenReturn(CLASS_NAME);
		when(source.getTypeName()).thenReturn(CLASS_NAME);

		final Iterable attributes = new ArrayList<CMAttribute>();
		when(theClass.getAttributes()).thenReturn(attributes);

		// when
		differ.updateCard(source, target);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView);
		inorder.verify(dataView).update(target);
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void ifSourceHasNoAttributesOfTargetDoNothing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		final CMCard target = mock(CMCard.class);
		final CMClass theClass = mock(CMClass.class);
		when(target.getType()).thenReturn(theClass);
		when(theClass.getName()).thenReturn(CLASS_NAME);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(Attribute.NULL_ATTRIBUTE);

		final Iterable attributes = new ArrayList<CMAttribute>();
		final CMAttribute codeAttribute = mock(CMAttribute.class);
		((ArrayList<CMAttribute>) attributes).add(codeAttribute);
		when(theClass.getAttributes()).thenReturn(attributes);
		when(codeAttribute.getName()).thenReturn(CODE_ATTRIBUTE);

		// when
		differ.updateCard(source, target);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView);
		inorder.verify(dataView).update(target);
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void ifSourceAndTargetHaveOneCommonAttributeWithSameValueDoNothing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		final CMCard target = mock(CMCard.class);
		final CMClass theClass = mock(CMClass.class);
		when(target.getType()).thenReturn(theClass);
		when(theClass.getName()).thenReturn(CLASS_NAME);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		final Attribute codeAttributeSource = mock(Attribute.class);
		when(codeAttributeSource.isValid()).thenReturn(true);
		when(codeAttributeSource.getValue()).thenReturn(THECODE);

		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(codeAttributeSource);
		when(target.get(CODE_ATTRIBUTE)).thenReturn(THECODE);

		final Iterable attributes = new ArrayList<CMAttribute>();
		final CMAttribute codeAttribute = mock(CMAttribute.class);
		((ArrayList<CMAttribute>) attributes).add(codeAttribute);
		when(theClass.getAttributes()).thenReturn(attributes);
		when(codeAttribute.getName()).thenReturn(CODE_ATTRIBUTE);
		final CMAttributeType attributeType = mock(CMAttributeType.class);
		when(codeAttribute.getType()).thenReturn(attributeType);

		when(attributeType.convertValue(THECODE)).thenReturn(THECODE);
		final CMCardDefinition cardDefinition = mock(CMCardDefinition.class);
		when(dataView.update(target)).thenReturn(cardDefinition);

		// when
		differ.updateCard(source, target);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView, cardDefinition);
		inorder.verify(dataView).update(target);
		verifyNoMoreInteractions(dataView, cardDefinition);
		// inorder.verify(cardDefinition).set(CODE_ATTRIBUTE, THECODE);
	}

	@Test
	public void ifSourceAndTargetHaveOneCommonAttributeWithDifferentValuesThenUpdateTarget() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		final CMCard target = mock(CMCard.class);
		final CMClass theClass = mock(CMClass.class);
		when(target.getType()).thenReturn(theClass);
		when(theClass.getName()).thenReturn(CLASS_NAME);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		final Attribute codeAttributeSource = mock(Attribute.class);
		when(codeAttributeSource.isValid()).thenReturn(true);
		when(codeAttributeSource.getValue()).thenReturn(THECODE);

		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(codeAttributeSource);
		when(target.get(CODE_ATTRIBUTE)).thenReturn(OTHER_CODE);

		final Iterable attributes = new ArrayList<CMAttribute>();
		final CMAttribute codeAttribute = mock(CMAttribute.class);
		((ArrayList<CMAttribute>) attributes).add(codeAttribute);
		when(theClass.getAttributes()).thenReturn(attributes);
		when(codeAttribute.getName()).thenReturn(CODE_ATTRIBUTE);
		final CMAttributeType attributeType = mock(CMAttributeType.class);
		when(codeAttribute.getType()).thenReturn(attributeType);

		when(attributeType.convertValue(THECODE)).thenReturn(THECODE);
		final CMCardDefinition cardDefinition = mock(CMCardDefinition.class);
		when(dataView.update(target)).thenReturn(cardDefinition);

		// when
		differ.updateCard(source, target);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView, cardDefinition);
		inorder.verify(dataView).update(target);
		inorder.verify(cardDefinition).set(CODE_ATTRIBUTE, THECODE);
		inorder.verify(cardDefinition).save();
		verifyNoMoreInteractions(dataView, cardDefinition);
	}

	@Test
	public void ifSourceHasNullValueAttributeUpdateNotNullValueOfTarget() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		final CMCard target = mock(CMCard.class);
		final CMClass theClass = mock(CMClass.class);
		when(target.getType()).thenReturn(theClass);
		when(theClass.getName()).thenReturn(CLASS_NAME);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		final Attribute codeAttributeSource = mock(Attribute.class);
		when(codeAttributeSource.isValid()).thenReturn(true);
		when(codeAttributeSource.getValue()).thenReturn(null);

		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(codeAttributeSource);
		when(target.get(CODE_ATTRIBUTE)).thenReturn(OTHER_CODE);

		final Iterable attributes = new ArrayList<CMAttribute>();
		final CMAttribute codeAttribute = mock(CMAttribute.class);
		((ArrayList<CMAttribute>) attributes).add(codeAttribute);
		when(theClass.getAttributes()).thenReturn(attributes);
		when(codeAttribute.getName()).thenReturn(CODE_ATTRIBUTE);
		final CMAttributeType attributeType = mock(CMAttributeType.class);
		when(codeAttribute.getType()).thenReturn(attributeType);

		when(attributeType.convertValue(THECODE)).thenReturn(THECODE);
		final CMCardDefinition cardDefinition = mock(CMCardDefinition.class);
		when(dataView.update(target)).thenReturn(cardDefinition);

		// when
		differ.updateCard(source, target);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView, cardDefinition);
		inorder.verify(dataView).update(target);
		inorder.verify(cardDefinition).set(CODE_ATTRIBUTE, null);
		inorder.verify(cardDefinition).save();
		verifyNoMoreInteractions(dataView, cardDefinition);
	}

	@Test
	public void ifSourceAndTargetHaveBothNullValueAttributeDoNOthing() throws Exception {
		// given
		final Entity source = mock(Entity.class);
		final CMCard target = mock(CMCard.class);
		final CMClass theClass = mock(CMClass.class);
		when(target.getType()).thenReturn(theClass);
		when(theClass.getName()).thenReturn(CLASS_NAME);
		when(source.getTypeName()).thenReturn(CLASS_NAME);
		final Attribute codeAttributeSource = mock(Attribute.class);
		when(codeAttributeSource.isValid()).thenReturn(true);
		when(codeAttributeSource.getValue()).thenReturn(null);

		when(source.getAttributeByName(CODE_ATTRIBUTE)).thenReturn(codeAttributeSource);
		when(target.get(CODE_ATTRIBUTE)).thenReturn(null);

		final Iterable attributes = new ArrayList<CMAttribute>();
		final CMAttribute codeAttribute = mock(CMAttribute.class);
		((ArrayList<CMAttribute>) attributes).add(codeAttribute);
		when(theClass.getAttributes()).thenReturn(attributes);
		when(codeAttribute.getName()).thenReturn(CODE_ATTRIBUTE);
		final CMAttributeType attributeType = mock(CMAttributeType.class);
		when(codeAttribute.getType()).thenReturn(attributeType);

		when(attributeType.convertValue(null)).thenReturn(null);
		final CMCardDefinition cardDefinition = mock(CMCardDefinition.class);
		when(dataView.update(target)).thenReturn(cardDefinition);

		// when
		differ.updateCard(source, target);

		// then
		verifyZeroInteractions(mapperRules, lookupLogic);
		final InOrder inorder = inOrder(dataView, cardDefinition);
		inorder.verify(dataView).update(target);
		verifyNoMoreInteractions(dataView, cardDefinition);
	}
}
