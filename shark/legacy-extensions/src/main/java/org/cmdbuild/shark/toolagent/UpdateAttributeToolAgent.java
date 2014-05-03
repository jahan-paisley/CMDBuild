package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.workflow.type.ReferenceType;

public class UpdateAttributeToolAgent extends ManageCardToolAgent {

	private static final String OBJ_ID = "ObjId";
	private static final String OBJ_REF = "ObjRef";

	private static final String UPDATE_ATTRIBUTE = "updateAttribute";
	private static final String UPDATE_ATTRIBUTE_REF = "updateAttributeRef";

	private static final String CLASS_NAME = "ClassName";
	private static final String ATTRIBUTE_NAME = "AttributeName";
	private static final String ATTRIBUTE_VALUE = "AttributeValue";
	private static final String DONE = "Done";

	private static final List<String> NOT_META_TOOLS = asList(UPDATE_ATTRIBUTE, UPDATE_ATTRIBUTE_REF);
	private static final List<String> NOT_META_ATTRIBUTES = asList(CLASS_NAME, OBJ_ID, OBJ_REF);

	@Override
	protected void innerInvoke() throws Exception {
		final ExistingCard existingCard = existingCard();
		for (final Entry<String, Object> attribute : getAttributeMap().entrySet()) {
			existingCard.withAttribute(attribute.getKey(), attribute.getValue());
		}
		existingCard.update();
		setParameterValue(DONE, true);
	}

	private ExistingCard existingCard() {
		final int cardId;
		String className = getExtendedAttribute(CLASS_NAME);
		if (className != null) {
			final Long objId = getParameterValue(OBJ_ID);
			cardId = objId.intValue();
		} else {
			if (hasParameter(CLASS_NAME)) {
				className = getParameterValue(CLASS_NAME);
				final Long objId = getParameterValue(OBJ_ID);
				cardId = objId.intValue();
			} else {
				final ReferenceType objReference = getParameterValue(OBJ_REF);
				className = getWorkflowApi().findClass(objReference.getIdClass()).getName();
				cardId = objReference.getId();
			}
		}
		return getWorkflowApi().existingCard(className, cardId);
	}

	@Override
	protected List<String> notMetaToolNames() {
		return NOT_META_TOOLS;
	}

	@Override
	protected List<String> fixedMetaAttributeNames() {
		return NOT_META_ATTRIBUTES;
	}

	@Override
	protected Map<String, Object> getAttributesForNonMetaInvoke() {
		final Map<String, Object> attributes = new HashMap<String, Object>();
		final String attributeName = getParameterValue(ATTRIBUTE_NAME);
		final String attributeValue = getParameterValue(ATTRIBUTE_VALUE);
		attributes.put(attributeName, attributeValue);
		return attributes;
	}

}
