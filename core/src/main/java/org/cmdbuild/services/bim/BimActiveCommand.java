package org.cmdbuild.services.bim;


public class BimActiveCommand extends BimDataModelCommand {

	public BimActiveCommand(BimPersistence bimDataPersistence, BimDataModelManager dataModelManager) {
		super(bimDataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {

		if (Boolean.parseBoolean(value)) {
			dataModelManager.createBimTableIfNeeded(className);
		}
		dataPersistence.saveActiveFlag(className, value);
	}

}
