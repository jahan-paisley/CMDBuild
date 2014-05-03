package org.cmdbuild.data.store;

public class StorableUtils {

	private static class StorableById implements Storable {

		private final Long id;

		public StorableById(final Long id) {
			this.id = id;
		}

		@Override
		public String getIdentifier() {
			return id.toString();
		}

	}

	private StorableUtils() {
		// prevents instantiation
	}

	public static Storable storableById(final Long id) {
		return new StorableById(id);
	}

}
