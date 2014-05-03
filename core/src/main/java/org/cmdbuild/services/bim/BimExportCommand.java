package org.cmdbuild.services.bim;

public class BimExportCommand extends BimDataModelCommand {

	public BimExportCommand(BimPersistence dataPersistence, BimDataModelManager dataModelManager) {
		super(dataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		// If value=true, first of all perform ActiveCommand and disable the export layer.
		if (Boolean.parseBoolean(value)) {
			BimActiveCommand activeCommand = new BimActiveCommand(dataPersistence, dataModelManager);
			activeCommand.execute(className, value);
			
			BimContainerCommand containerCommand = new BimContainerCommand(dataPersistence, dataModelManager);
			containerCommand.execute(className, "false");
			
			dataModelManager.addPositionFieldIfNeeded(className);
		}
		dataPersistence.saveExportFlag(className, value);
	}

}
