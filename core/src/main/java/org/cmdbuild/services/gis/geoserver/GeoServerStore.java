package org.cmdbuild.services.gis.geoserver;

import org.restlet.data.MediaType;

public class GeoServerStore {

	public static enum StoreType {
		DATA_STORE("dataStore", "featureType"), COVERAGE_STORE("coverageStore", "coverage");

		private final String name;
		private final String subelementName;

		private StoreType(final String name, final String subelementName) {
			this.name = name;
			this.subelementName = subelementName;
		}

		public String getName() {
			return name;
		}

		public String getSubelementName() {
			return subelementName;
		}
	}

	public static enum StoreDataType {
		SHAPE(StoreType.DATA_STORE, "Shapefile", "shp", MediaType.APPLICATION_ZIP), WORLDIMAGE(
				StoreType.COVERAGE_STORE, "WorldImage", "worldimage", MediaType.APPLICATION_ZIP), GEOTIFF(
				StoreType.COVERAGE_STORE, "GeoTIFF", "geotiff", MediaType.IMAGE_TIFF);

		private final StoreType storeType;
		private final String subtype;
		private final String uploadFileExtension;
		private final MediaType mime;

		private StoreDataType(final StoreType storeType, final String subtype, final String uploadFileExtension,
				final MediaType mime) {
			this.storeType = storeType;
			this.subtype = subtype;
			this.uploadFileExtension = uploadFileExtension;
			this.mime = mime;
		}

		public String getUploadFileExtension() {
			return uploadFileExtension;
		}

		public StoreType getStoreType() {
			return storeType;
		}

		public String getStoreTypeName() {
			return storeType.getName();
		}

		public String getStoreSubtypeName() {
			return storeType.getSubelementName();
		}

		public String getStoreSubtype() {
			return subtype;
		}

		public MediaType getMime() {
			return mime;
		}
	}

	private final String name;
	private final StoreDataType storeDataType;

	public GeoServerStore(final String name, final StoreDataType dataType) {
		this.name = name;
		this.storeDataType = dataType;
	}

	public String getName() {
		return name;
	}

	public StoreDataType getDataType() {
		return storeDataType;
	}

	public String getStoreType() {
		return storeDataType.getStoreTypeName();
	}

	public String getStoreSubtype() {
		return storeDataType.getStoreSubtypeName();
	}

	@Override
	public String toString() {
		return getName();
	}
}
