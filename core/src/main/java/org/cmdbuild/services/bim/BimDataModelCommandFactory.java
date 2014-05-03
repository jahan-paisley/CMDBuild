package org.cmdbuild.services.bim;

public class BimDataModelCommandFactory {

	private final BimPersistence dataPersistence;
	private final BimDataModelManager dataModelManager;

	public BimDataModelCommandFactory(final BimPersistence dataPersistence, final BimDataModelManager dataModelManager) {
		this.dataPersistence = dataPersistence;
		this.dataModelManager = dataModelManager;
	}

	public BimDataModelCommand create(final String attributeName) {
		return LayerUpdater.of(attributeName).create(dataPersistence, dataModelManager);
	}

	private static enum LayerUpdater {
		active {
			@Override
			public BimDataModelCommand create(final BimPersistence dataPersistence,
					final BimDataModelManager dataModelManager) {
				return new BimActiveCommand(dataPersistence, dataModelManager);
			}
		}, //
		root {
			@Override
			public BimDataModelCommand create(final BimPersistence bimDataPersistence,
					final BimDataModelManager dataModelManager) {
				return new BimRootCommand(bimDataPersistence, dataModelManager);
			}
		}, //
		export {
			@Override
			public BimDataModelCommand create(final BimPersistence bimDataPersistence,
					final BimDataModelManager dataModelManager) {
				return new BimExportCommand(bimDataPersistence, dataModelManager);
			}
		}, //
		container {
			@Override
			public BimDataModelCommand create(final BimPersistence bimDataPersistence,
					final BimDataModelManager dataModelManager) {
				return new BimContainerCommand(bimDataPersistence, dataModelManager);
			}
		}, //
		rootreference {
			@Override
			public BimDataModelCommand create(final BimPersistence bimDataPersistence,
					final BimDataModelManager dataModelManager) {
				return new BimRootReferenceCommand(bimDataPersistence, dataModelManager);
			}
		},
		unknown {
			@Override
			public BimDataModelCommand create(final BimPersistence bimDataPersistence,
					final BimDataModelManager dataModelManager) {
				return new BimDataModelCommand(bimDataPersistence, dataModelManager) {

					@Override
					public void execute(final String className, final String value) {
						// TODO Auto-generated method stub
					}
				};
			}
		}, //
		;

		public static LayerUpdater of(final String attributeName) {
			for (final LayerUpdater attribute : values()) {
				if (attribute.name().equals(attributeName)) {
					return attribute;
				}
			}
			return unknown;
		}

		public abstract BimDataModelCommand create(BimPersistence dataPersistence, BimDataModelManager dataModelManager);
	}

}