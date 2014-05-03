package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.bim.utils.BimConstants.isValidId;

import java.util.Map;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.connector.export.DataChangedListener.DataChangedException;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

public class ExportConnectorFramework implements ConnectorFramework {

	final private GenericMapper connector;
	final private BimPersistence bimPersistence;
	final private BimFacade bimServiceFacade;

	public ExportConnectorFramework(final BimDataView dataView, final BimFacade bimServiceFacade,
			final BimPersistence bimPersistence, final ExportPolicy exportProjectPolicy) {
		this.bimPersistence = bimPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.connector = new DefaultExportConnector(dataView, bimServiceFacade, bimPersistence, exportProjectPolicy);
	}

	@Override
	public boolean isSynch(final Object input) {
		boolean synch = true;
		final Output changeListener = new DataChangedListener();
		try {
			performExport(input, changeListener);
		} catch (final DataChangedException de) {
			synch = false;
		}
		return synch;
	}

	private void performExport(final Object input, final Output output) {
		final String sourceProjectId = String.class.cast(input);

		connector.setConfiguration(sourceProjectId);

		connector.setTarget(sourceProjectId, output);

		final Map<String, Entity> sourceData = connector.getSourceData();

		final Map<String, Entity> targetData = connector.getTargetData();

		final MapDifference<String, Entity> difference = Maps.difference(sourceData, targetData);
		final Map<String, Entity> entriesToCreate = difference.entriesOnlyOnLeft();
		final Map<String, ValueDifference<Entity>> entriesToUpdate = difference.entriesDiffering();
		final Map<String, Entity> entriesToRemove = difference.entriesOnlyOnRight();

		if (entriesToCreate.isEmpty() && entriesToUpdate.isEmpty() && entriesToRemove.isEmpty()) {
			return;
		}

		try {
			connector.beforeExecution();

			connector.executeSynchronization(entriesToCreate, entriesToUpdate, entriesToRemove, output);

			connector.afterExecution(output);

		} catch (final Throwable t) {
			output.notifyError(t);
		}
	}

	@Override
	public void executeConnector(final Object input, final Output output) {
		final boolean isSynchronized = isSynch(input);
		System.out.println("Is synchronized? " + isSynchronized);
		if (!isSynchronized) {
			performExportSynchronized(input, output);
		}
	}

	private synchronized void performExportSynchronized(final Object input, final Output output) {
		performExport(input, output);
	}

	@Override
	public Object getLastGeneratedOutput(final Object input) {
		final String inputProjectId = String.class.cast(input);
		final String exportProjectId = bimPersistence.read(inputProjectId).getExportProjectId();
		if (!isValidId(exportProjectId)) {
			throw new BimError("Project for export not found");
		}
		final String outputRevisionId = bimServiceFacade.getLastRevisionOfProject(exportProjectId);
		return outputRevisionId;
	}
}
