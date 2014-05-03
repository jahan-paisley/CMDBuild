package org.cmdbuild.services.bim;

public class BimContainerCommand extends BimDataModelCommand {

	public BimContainerCommand(BimPersistence bimDataPersistence, BimDataModelManager dataModelManager) {
		super(bimDataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		// If value=true, first of all perform ActiveCommand.
		if (Boolean.parseBoolean(value)) {
			BimActiveCommand activeCommand = new BimActiveCommand(dataPersistence, dataModelManager);
			BimExportCommand exportCommand = new BimExportCommand(dataPersistence, dataModelManager);
			activeCommand.execute(className, value);
			exportCommand.execute(className, "false");
			dataModelManager.addPerimeterAndHeightFieldsIfNeeded(className);
		}
		dataPersistence.saveContainerFlag(className, value);
	}

}
