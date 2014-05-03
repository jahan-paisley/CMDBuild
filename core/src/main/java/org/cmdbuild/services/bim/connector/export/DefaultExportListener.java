package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.services.bim.DefaultBimDataView.CONTAINER_GUID;

import java.util.List;
import java.util.Map;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.services.bim.BimFacade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultExportListener implements Output {

	private final BimFacade serviceFacade;
	Map<String, Map<String, List<String>>> relationsMap = Maps.newHashMap();
	private final ExportPolicy exportPolicy;

	public DefaultExportListener(final BimFacade bimFacade, final ExportPolicy exportPolicy) {
		this.serviceFacade = bimFacade;
		this.exportPolicy = exportPolicy;
	}

	@Override
	public void createTarget(final Entity entityToCreate, final String targetProjectId) {
		final String objectOid = serviceFacade.createCard(entityToCreate, targetProjectId);
		System.out.println("object '" + objectOid + "' created");
		final String spaceGuid = entityToCreate.getAttributeByName(CONTAINER_GUID).getValue();
		toAdd(objectOid, spaceGuid);
	}

	@Override
	public void deleteTarget(final Entity entityToRemove, final String targetProjectId) {
		final String removedObjectOid = serviceFacade.removeCard(entityToRemove, targetProjectId);
		System.out.println("object '" + removedObjectOid + "' removed");
		final String oldContainerOid = entityToRemove.getAttributeByName(CONTAINER_GUID).getValue();
		if (!oldContainerOid.isEmpty()) {
			toRemove(removedObjectOid, oldContainerOid);
		}
	}

	private void toAdd(final String objectOid, final String spaceGuid) {
		if (relationsMap.containsKey(spaceGuid)) {
			final Map<String, List<String>> spaceMap = relationsMap.get(spaceGuid);
			if (spaceMap.containsKey("A")) {
				spaceMap.get("A").add(objectOid);
			} else {
				final List<String> listToAdd = Lists.newArrayList(objectOid);
				spaceMap.put("A", listToAdd);
			}
		} else {
			final Map<String, List<String>> spaceMap = Maps.newHashMap();
			final List<String> listToAdd = Lists.newArrayList(objectOid);
			spaceMap.put("A", listToAdd);
			relationsMap.put(spaceGuid, spaceMap);
		}
	}

	private void toRemove(final String objectOid, final String spaceGuid) {
		if (relationsMap.containsKey(spaceGuid)) {
			final Map<String, List<String>> spaceMap = relationsMap.get(spaceGuid);
			if (spaceMap.containsKey("D")) {
				spaceMap.get("D").add(objectOid);
			} else {
				final List<String> listToAdd = Lists.newArrayList(objectOid);
				spaceMap.put("D", listToAdd);
			}
		} else {
			final Map<String, List<String>> spaceMap = Maps.newHashMap();
			final List<String> listToAdd = Lists.newArrayList(objectOid);
			spaceMap.put("D", listToAdd);
			relationsMap.put(spaceGuid, spaceMap);
		}
	}

	@Override
	public void finalActions(final String targetProjectId) {
		serviceFacade.updateRelations(relationsMap, targetProjectId);
		relationsMap = Maps.newHashMap();
	}

	@Override
	public void outputInvalid(final String outputId) {
		exportPolicy.beforeExport(outputId);
	}

	@Override
	public void notifyError(final Throwable t) {
		serviceFacade.abortTransaction();
		throw new BimError("Export failed", t);
	}
}
