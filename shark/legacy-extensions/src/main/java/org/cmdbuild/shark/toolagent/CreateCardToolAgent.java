package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.common.Constants;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class CreateCardToolAgent extends ManageCardToolAgent {

	private static final String CREATE_CARD = "createCard";
	private static final String CREATE_CARD_REF = "createCardRef";

	private static final String CLASS_NAME = "ClassName";
	private static final String CARD_CODE = "CardCode";
	private static final String CARD_DESCRIPTION = "CardDescription";

	private static final List<String> NOT_META_TOOLS = asList(CREATE_CARD, CREATE_CARD_REF);
	private static final List<String> NOT_META_ATTRIBUTES = asList(CLASS_NAME);

	@Override
	protected void innerInvoke() throws Exception {
		final String classname = getClassName();
		final Map<String, Object> attributes = getAttributeMap();
		final int newCardId = createCard(classname, attributes);

		for (final AppParameter parmOut : getReturnParameters()) {
			if (parmOut.the_class == Long.class) {
				parmOut.the_value = newCardId;
			} else if (parmOut.the_class == ReferenceType.class) {
				final String description = (String) attributes.get(Constants.DESCRIPTION_ATTRIBUTE);
				final ReferenceType reference = new ReferenceType();
				reference.setId(newCardId);
				reference.setIdClass(getWorkflowApi().findClass(classname).getId());
				reference.setDescription(description);
				parmOut.the_value = reference;
			}
		}
	}

	private final String getClassName() {
		String className = getExtendedAttribute(CLASS_NAME);
		if (className == null) {
			className = getParameterValue(CLASS_NAME);
		}
		return className;
	}

	private int createCard(final String classname, final Map<String, Object> attributes) {
		final NewCard newCard = getWorkflowApi().newCard(classname);
		for (final Entry<String, Object> attribute : attributes.entrySet()) {
			newCard.with(attribute.getKey(), attribute.getValue());
		}
		final CardDescriptor cardDescriptor = newCard.create();
		final int newCardId = cardDescriptor.getId();
		return newCardId;
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
		final String code = getParameterValue(CARD_CODE);
		attributes.put(Constants.CODE_ATTRIBUTE, code);
		final String description = getParameterValue(CARD_DESCRIPTION);
		attributes.put(Constants.DESCRIPTION_ATTRIBUTE, description);
		return attributes;
	}

}
