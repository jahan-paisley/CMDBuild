package org.cmdbuild.services.bim;

public abstract class BimDataModelCommand {

	protected final BimPersistence dataPersistence;
	protected final BimDataModelManager dataModelManager;

	public BimDataModelCommand(BimPersistence dataPersistence, BimDataModelManager modelManager) {
		this.dataPersistence = dataPersistence;
		this.dataModelManager = modelManager;
	}

	public abstract void execute(String className, String value);

}
