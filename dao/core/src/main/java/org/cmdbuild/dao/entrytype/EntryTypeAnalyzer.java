package org.cmdbuild.dao.entrytype;

import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.BASE_PROCESS_CLASS_NAME;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.collect.Lists;

public class EntryTypeAnalyzer {

	private final CMEntryType entryType;
	private final CMDataView view;

	private EntryTypeAnalyzer(final CMEntryType entryType, final CMDataView view) {
		this.entryType = entryType;
		this.view = view;
	}

	public static EntryTypeAnalyzer inspect(final CMEntryType entryType, final CMDataView view) {
		Validate.notNull(entryType);
		Validate.notNull(view);
		return new EntryTypeAnalyzer(entryType, view);
	}

	/**
	 * 
	 * @return true if the entry type has at least one ACTIVE and NOT SYSTEM
	 *         attribute of one of the following types: Lookup, Reference,
	 *         ForeignKey; false otherwise
	 */
	public boolean hasExternalReferences() {
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			final CMAttributeType<?> attributeType = attribute.getType();
			if (attributeType instanceof LookupAttributeType || //
					attributeType instanceof ReferenceAttributeType || //
					attributeType instanceof ForeignKeyAttributeType) {
				return true;
			}
		}
		return false;
	}

	public Iterable<CMAttribute> getLookupAttributes() {
		final List<CMAttribute> lookupAttributes = Lists.newArrayList();
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			if (attribute.getType() instanceof LookupAttributeType) {
				lookupAttributes.add(attribute);
			}
		}
		return lookupAttributes;
	}

	public Iterable<CMAttribute> getReferenceAttributes() {
		final List<CMAttribute> referenceAttributes = Lists.newArrayList();
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			if (attribute.getType() instanceof ReferenceAttributeType) {
				referenceAttributes.add(attribute);
			}
		}
		return referenceAttributes;
	}

	public Iterable<CMAttribute> getForeignKeyAttributes() {
		final List<CMAttribute> foreignKeyAttributes = Lists.newArrayList();
		if (!entryType.holdsHistory()) { // entry type is a simple class
			for (final CMAttribute attribute : entryType.getActiveAttributes()) {
				if (attribute.getType() instanceof ForeignKeyAttributeType) {
					foreignKeyAttributes.add(attribute);
				}
			}
		}
		return foreignKeyAttributes;
	}

	public boolean isSimpleClass() {
		final CMClass baseClass = view.findClass(BASE_CLASS_NAME);
		return !baseClass.isAncestorOf((CMClass) entryType);
	}

	public boolean isStandardClass() {
		final CMClass baseClass = view.findClass(BASE_CLASS_NAME);
		return baseClass.isAncestorOf((CMClass) entryType);
	}

	public boolean isProcessClass() {
		final CMClass baseProcessClass = view.findClass(BASE_PROCESS_CLASS_NAME);
		return baseProcessClass.isAncestorOf((CMClass) entryType);
	}

}
