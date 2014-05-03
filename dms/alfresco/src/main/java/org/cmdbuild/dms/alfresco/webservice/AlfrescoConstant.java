package org.cmdbuild.dms.alfresco.webservice;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.apache.commons.lang3.ArrayUtils;
import org.cmdbuild.dms.StoredDocument;

enum AlfrescoConstant {

	NULL(EMPTY) {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			// nothing to do
		}
	},
	NAME(Constants.PROP_NAME) {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setName(namedValue.getValue());
		}
	},
	CREATED(Constants.PROP_CREATED) {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setCreated(DateUtils.parse(namedValue.getValue()));
		}
	},
	DESCR(Constants.PROP_DESCRIPTION) {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setDescription(namedValue.getValue());
		}
	},
	MODIFIED("{http://www.alfresco.org/model/content/1.0}modified") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setModified(DateUtils.parse(namedValue.getValue()));
		}
	},
	CATEGORIES("{http://www.alfresco.org/model/content/1.0}categories") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			final String[] paths = namedValue.getValues();
			final String strip = "workspace://SpacesStore/";
			for (String uuid : (paths == null) ? ArrayUtils.EMPTY_STRING_ARRAY : paths) {
				final int idx = uuid.indexOf(strip);
				uuid = uuid.substring(idx + strip.length());
				final ResultSetRow row = client.searchRow(uuid);
				if (row != null) {
					final NamedValue[] namedValues = row.getColumns();
					for (final NamedValue nv : namedValues) {
						if (NAME.isName(nv.getName())) {
							storedDocument.setCategory(nv.getValue());
						}
					}
				}
			}
		}
	},
	VERSION("{http://www.alfresco.org/model/content/1.0}versionLabel") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setVersion(namedValue.getValue());
		}
	},
	PATH("{http://www.alfresco.org/model/content/1.0}path") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setPath(namedValue.getValue());
		}
	},
	AUTHOR("{http://www.alfresco.org/model/content/1.0}author") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setAuthor(namedValue.getValue());
		}
	},
	UUID("{http://www.alfresco.org/model/system/1.0}node-uuid") {
		@Override
		public void setInBean(final StoredDocument storedDocument, final NamedValue namedValue,
				final AlfrescoWebserviceClient client) {
			storedDocument.setUuid(namedValue.getValue());
		}
	};

	private final String name;

	private AlfrescoConstant(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isName(final String name) {
		return this.name.equals(name);
	}

	public static AlfrescoConstant from(final NamedValue namedValue) {
		for (final AlfrescoConstant ac : AlfrescoConstant.values()) {
			if (ac.isName(namedValue.getName())) {
				return ac;
			}
		}
		return NULL;
	}

	public abstract void setInBean(StoredDocument storedDocument, NamedValue namedValue, AlfrescoWebserviceClient client);

}
