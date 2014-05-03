package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.StorableLayer;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class BimRootCommand extends BimDataModelCommand {

	public BimRootCommand(BimPersistence dataPersistence, BimDataModelManager dataModelManager) {
		super(dataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		if (Boolean.parseBoolean(value)) {
			BimActiveCommand activeCommand = new BimActiveCommand(dataPersistence, dataModelManager);
			activeCommand.execute(className, value);
			StorableLayer oldBimRoot = dataPersistence.findRoot();
			if (oldBimRoot != null && !isEmpty(oldBimRoot.getClassName())
					&& !oldBimRoot.getClassName().equals(className)) {
				dataModelManager.deleteBimDomainIfExists(oldBimRoot.getClassName());
				dataPersistence.saveRootFlag(oldBimRoot.getClassName(), false);
				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.saveRootFlag(className, true);
			} else if (oldBimRoot == null || isEmpty(oldBimRoot.getClassName())) {
				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.saveRootFlag(className, true);
			}
		} else {
			dataModelManager.deleteBimDomainIfExists(className); 
			dataPersistence.saveRootFlag(className, false);
		}
	}

}
