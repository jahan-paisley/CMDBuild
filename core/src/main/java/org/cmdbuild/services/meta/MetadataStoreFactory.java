package org.cmdbuild.services.meta;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.MetadataConverter;
import org.cmdbuild.data.converter.MetadataGroupable;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.data.Metadata;

public class MetadataStoreFactory {

	private final CMDataView dataView;

	public MetadataStoreFactory(final CMDataView dataView) {
		this.dataView = dataView;
	}

	public Store<Metadata> storeForAttribute(final CMAttribute attribute) {
		return DataViewStore.newInstance( //
				dataView, //
				MetadataGroupable.of(attribute), //
				MetadataConverter.of(attribute));
	}

}
