package org.cmdbuild.services.bim;

public class BimRootReferenceCommand extends BimDataModelCommand {

	public BimRootReferenceCommand(BimPersistence bimDataPersistence, BimDataModelManager dataModelManager) {
		super(bimDataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		dataPersistence.saveRootReferenceName(className, value);

	}

}
