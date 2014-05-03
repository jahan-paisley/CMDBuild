package org.cmdbuild.services.bim.connector;

import java.util.Iterator;
import java.util.Map;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.bim.BimDataView;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

public class OptimizedDefaultCardDiffer implements CardDiffer {

	private final CMDataView dataView;
	private final BimDataView bimDataView;
	private final LookupLogic lookupLogic;
	private static final Logger logger = LoggerSupport.logger;

	public OptimizedDefaultCardDiffer(final CMDataView dataView, final LookupLogic lookupLogic,
			BimDataView bimDataView) {
		this.dataView = dataView;
		this.bimDataView = bimDataView;
		this.lookupLogic = lookupLogic;
	}

	@Override
	public CMCard updateCard(final Entity sourceEntity, final CMCard oldCard) {
		CMCard updatedCard = null;
		final CMClass theClass = oldCard.getType();
		final String className = theClass.getName();
		if (!className.equals(sourceEntity.getTypeName())) {
			// better safe than sorry...
			return updatedCard;
		}
		final CMCardDefinition cardDefinition = dataView.update(oldCard);
		logger.info("Updating card " + oldCard.getId() + " of type " + className);
		boolean sendDelta = false;
		Map<String, CMAttribute> cmAttributesMap = Maps.newHashMap();
		logger.debug("Build attributes map...");
		for (final CMAttribute attribute : theClass.getAttributes()) {
			if (!cmAttributesMap.containsKey(attribute.getName())) {
				cmAttributesMap.put(attribute.getName(), attribute);
			}
		}
		logger.debug("Success.");
		for (final String attributeName : sourceEntity.getAttributes().keySet()) {
			if(!cmAttributesMap.containsKey(attributeName)){
				continue;
			}
			final Attribute attribute = sourceEntity.getAttributeByName(attributeName);
			if (attribute.isValid()) {
				logger.info("attribute '{}' found", attributeName);
				CMAttribute cmAttribute = cmAttributesMap.get(attributeName);
				final CMAttributeType<?> attributeType = cmAttribute.getType();
				final boolean isReference = attributeType instanceof ReferenceAttributeType;
				final boolean isLookup = attributeType instanceof LookupAttributeType;
				final Object oldAttributeValue = oldCard.get(attributeName);
				if (isReference || isLookup) {
					final IdAndDescription oldReference = (IdAndDescription) oldAttributeValue;
					Long newReferencedId = null;
					if (isReference) {
						final String referencedClass = findReferencedClassNameFromReferenceAttribute(cmAttribute);
						final String newReferencedKey = sourceEntity.getAttributeByName(attributeName).getValue();
						newReferencedId = bimDataView.getIdFromGlobalId(newReferencedKey, referencedClass);
					} else if (isLookup) {
						final String lookupType = ((LookupAttributeType) cmAttribute.getType()).getLookupTypeName();
						final String newLookupValue = sourceEntity.getAttributeByName(attributeName).getValue();
						newReferencedId = findLookupIdFromDescription(newLookupValue, lookupType);
					}
					if (newReferencedId != null && !newReferencedId.equals(oldReference.getId())) {
						final IdAndDescription newReference = new IdAndDescription(newReferencedId, "");
						cardDefinition.set(attributeName, newReference);
						sendDelta = true;
					}
				} else {
					final Object newAttributeValue = attributeType.convertValue(sourceEntity.getAttributeByName(
							attributeName).getValue());
					if ((newAttributeValue != null && !newAttributeValue.equals(oldAttributeValue))
							|| (newAttributeValue == null && oldAttributeValue != null)) {
						cardDefinition.set(attributeName, newAttributeValue);
						sendDelta = true;
					}
				}
			}
		}
		if (sendDelta) {
			updatedCard = cardDefinition.save();
			logger.info("Card updated");
		}
		return updatedCard;
	}

	@Override
	public CMCard createCard(final Entity sourceEntity) {
		CMCard newCard = null;
		final String className = sourceEntity.getTypeName();
		final CMClass theClass = dataView.findClass(className);
		if (theClass == null) {
			logger.warn("Class " + className + " not found");
			return null;
		}
		logger.info("Building card of type " + theClass.getName());
		final CMCardDefinition cardDefinition = dataView.createCardFor(theClass);
		Map<String, CMAttribute> cmAttributesMap = Maps.newHashMap();
		logger.debug("Build attributes map...");
		for (final CMAttribute attribute : theClass.getAttributes()) {
			if (!cmAttributesMap.containsKey(attribute.getName())) {
				cmAttributesMap.put(attribute.getName(), attribute);
			}
		}
		logger.debug("Success.");
		boolean sendDelta = false;
		for (final String attributeName : sourceEntity.getAttributes().keySet()) {
			final Attribute attribute = sourceEntity.getAttributeByName(attributeName);
			if(!cmAttributesMap.containsKey(attributeName)){
				continue;
			}
			if (attribute.isValid()) {
				logger.info("attribute '{}' found", attributeName);
				CMAttribute cmAttribute = cmAttributesMap.get(attributeName);
				final CMAttributeType<?> attributeType = cmAttribute.getType();
				final boolean isReference = attributeType instanceof ReferenceAttributeType;
				final boolean isLookup = attributeType instanceof LookupAttributeType;
				if (attribute.isValid()) {
					if (isReference || isLookup) {
						Long newReferencedId = null;
						if (isReference) {
							final String referencedClass = findReferencedClassNameFromReferenceAttribute(cmAttribute);
							final String referencedGuid = attribute.getValue();
							newReferencedId = bimDataView.getIdFromGlobalId(referencedGuid, referencedClass);
						} else if (isLookup) {
							final String newLookupValue = attribute.getValue();
							final String lookupType = ((LookupAttributeType) attributeType).getLookupTypeName();
							newReferencedId = findLookupIdFromDescription(newLookupValue, lookupType);
						}
						if (newReferencedId != null) {
							attribute.setValue(newReferencedId.toString());
							cardDefinition.set(attributeName, attribute.getValue());
							sendDelta = true;
						}
					} else {
						cardDefinition.set(attributeName, attribute.getValue());
						sendDelta = true;
					}
				}
			}
		}
		if (sendDelta) {
			newCard = cardDefinition.save();
		}
		return newCard;
	}

	private String findReferencedClassNameFromReferenceAttribute(final CMAttribute attribute) {
		final String domainName = ((ReferenceAttributeType) attribute.getType()).getDomainName();
		final CMDomain domain = dataView.findDomain(domainName);
		String referencedClass = "";
		final String ownerClassName = attribute.getOwner().getName();
		if (domain.getClass1().getName().equals(ownerClassName)) {
			referencedClass = domain.getClass2().getName();
		} else {
			referencedClass = domain.getClass1().getName();
		}
		return referencedClass;
	}

	private Long findLookupIdFromDescription(final String lookupValue, final String lookupType) {
		Long lookupId = null;
		final Iterable<LookupType> allLookupTypes = lookupLogic.getAllTypes();
		LookupType theType = null;
		for (final Iterator<LookupType> it = allLookupTypes.iterator(); it.hasNext();) {
			final LookupType lt = it.next();
			if (lt.name.equals(lookupType)) {
				theType = lt;
				break;
			}
		}
		final Iterable<Lookup> allLookusOfType = lookupLogic.getAllLookup(theType, true);

		for (final Iterator<Lookup> it = allLookusOfType.iterator(); it.hasNext();) {
			final Lookup l = it.next();
			if (l.getDescription() != null && l.getDescription().equals(lookupValue)) {
				lookupId = l.getId();
				break;
			}
		}
		return lookupId;
	}

}
