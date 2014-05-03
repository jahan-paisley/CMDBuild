package org.cmdbuild.cmdbf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QNameType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;

public class CMDBfItem {
	private Set<CMDBfId> idSet;
	private Set<QNameType> additionalRecordTypeSet;
	private List<RecordType> recordList;

	public CMDBfItem(final CMDBfId id) {
		addInstanceId(id);
	}

	protected CMDBfItem(final Collection<? extends MdrScopedIdType> idList) {
		for (final MdrScopedIdType id : idList) {
			addInstanceId(id);
		}
	}

	public CMDBfItem(final ItemType item) {
		this(item.getInstanceId());
		for (final RecordType record : item.getRecord()) {
			records().add(record);
		}
		for (final QNameType type : item.getAdditionalRecordType()) {
			additionalRecordTypes().add(type);
		}
	}

	public Collection<CMDBfId> instanceIds() {
		if (idSet == null) {
			idSet = new HashSet<CMDBfId>();
		}
		return idSet;
	}

	public Collection<RecordType> records() {
		if (recordList == null) {
			recordList = new ArrayList<RecordType>();
		}
		return recordList;
	}

	public Collection<QNameType> additionalRecordTypes() {
		if (additionalRecordTypeSet == null) {
			additionalRecordTypeSet = new HashSet<QNameType>();
		}
		return additionalRecordTypeSet;
	}

	@Override
	public boolean equals(final Object obj) {
		boolean equals = false;
		if (obj instanceof CMDBfItem) {
			final CMDBfItem item = (CMDBfItem) obj;
			final Iterator<CMDBfId> iterator = item.instanceIds().iterator();
			while (!equals && iterator.hasNext()) {
				equals = instanceIds().contains(iterator.next());
			}
		}
		return equals;
	}

	public boolean merge(final CMDBfItem item) {
		boolean modified = false;
		if (item != this) {
			modified = instanceIds().addAll(item.instanceIds());
			modified |= records().addAll(item.records());
			modified |= additionalRecordTypes().addAll(item.additionalRecordTypes());
		}
		return modified;
	}

	private boolean addInstanceId(MdrScopedIdType id) {
		if (!(id instanceof CMDBfId)) {
			id = new CMDBfId(id);
		}
		return instanceIds().add((CMDBfId) id);
	}
}