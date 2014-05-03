package org.cmdbuild.cmdbf;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.dmtf.schemas.cmdbf._1.tns.servicedata.PropertyValueType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QNameType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class RecordConstraintPredicate implements Predicate<RecordType> {

	private final RecordConstraintType recordConstraint;

	public RecordConstraintPredicate(final RecordConstraintType recordConstraint) {
		this.recordConstraint = recordConstraint;
	}

	@Override
	public boolean apply(final RecordType record) {
		boolean match = true;
		if (!recordConstraint.getRecordType().isEmpty()) {
			final QName recordType = CMDBfUtils.getRecordType(record);
			match = Iterables.any(recordConstraint.getRecordType(), new Predicate<QNameType>() {
				@Override
				public boolean apply(final QNameType input) {
					return input.getNamespace().equals(recordType.getNamespaceURI())
							&& input.getLocalName().equals(recordType.getLocalPart());
				}
			});
		}
		if (!recordConstraint.getPropertyValue().isEmpty()) {
			try {
				final Map<QName, String> properties = CMDBfUtils.parseRecord(record);
				match = Iterables.all(recordConstraint.getPropertyValue(), new Predicate<PropertyValueType>() {
					@Override
					public boolean apply(final PropertyValueType input) {
						return CMDBfUtils.filter(properties, input);
					}
				});
			} catch (final ParserConfigurationException e) {
				throw new Error(e);
			}
		}
		return match;
	}
}
