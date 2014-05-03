package org.cmdbuild.workflow;

import java.util.List;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.eventaudit.AssignmentEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.CreateProcessEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.DataEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.DeleteProcessEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.EventAuditException;
import org.enhydra.shark.api.internal.eventaudit.EventAuditManagerInterface;
import org.enhydra.shark.api.internal.eventaudit.PropertiesEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.StateEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

abstract class NullEventAuditManager implements EventAuditManagerInterface {

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
	}

	@Override
	public void delete(final WMSessionHandle shandle, final AssignmentEventAuditPersistenceObject assea)
			throws EventAuditException {
	}

	@Override
	public void delete(final WMSessionHandle shandle, final CreateProcessEventAuditPersistenceObject cpea)
			throws EventAuditException {
	}

	@Override
	public void delete(final WMSessionHandle shandle, final DataEventAuditPersistenceObject dea)
			throws EventAuditException {
	}

	@Override
	public void delete(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea)
			throws EventAuditException {
	}

	@Override
	public void delete(final WMSessionHandle shandle, final PropertiesEventAuditPersistenceObject pea)
			throws EventAuditException {
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List listActivityHistoryInfoWhere(final WMSessionHandle shandle, final String sqlWhere, final int startAt,
			final int limit, final boolean fillVariablesInfo) throws EventAuditException {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List listProcessDefinitionHistoryInfoWhere(final WMSessionHandle shandle, final String factoryName,
			final boolean countProcessInstances) throws EventAuditException {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List listProcessHistoryInfoWhere(final WMSessionHandle shandle, final String sqlWhere, final int startAt,
			final int limit, final boolean fillActivitiesInfo, final boolean fillActivitiyVariablesInfo,
			final boolean fillVariablesInfo) throws EventAuditException {
		return null;
	}

	@Override
	public void persist(final WMSessionHandle shandle, final AssignmentEventAuditPersistenceObject assea)
			throws EventAuditException {
	}

	@Override
	public void persist(final WMSessionHandle shandle, final CreateProcessEventAuditPersistenceObject cpea)
			throws EventAuditException {
	}

	@Override
	public void persist(final WMSessionHandle shandle, final DataEventAuditPersistenceObject dea)
			throws EventAuditException {
	}

	@Override
	public void persist(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea)
			throws EventAuditException {
	}

	@Override
	public void persist(final WMSessionHandle shandle, final PropertiesEventAuditPersistenceObject pea)
			throws EventAuditException {
	}

	@Override
	public void persist(final WMSessionHandle shandle, final DeleteProcessEventAuditPersistenceObject pea)
			throws EventAuditException {
	}

	@Override
	public boolean restore(final WMSessionHandle shandle, final AssignmentEventAuditPersistenceObject assea)
			throws EventAuditException {
		return false;
	}

	@Override
	public boolean restore(final WMSessionHandle shandle, final CreateProcessEventAuditPersistenceObject cpea)
			throws EventAuditException {
		return false;
	}

	@Override
	public boolean restore(final WMSessionHandle shandle, final DataEventAuditPersistenceObject dea)
			throws EventAuditException {
		return false;
	}

	@Override
	public boolean restore(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea)
			throws EventAuditException {
		return false;
	}

	@Override
	public boolean restore(final WMSessionHandle shandle, final PropertiesEventAuditPersistenceObject pea)
			throws EventAuditException {
		return false;
	}

	@Override
	public boolean restore(final WMSessionHandle shandle, final DeleteProcessEventAuditPersistenceObject pea)
			throws EventAuditException {
		return false;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List restoreActivityHistory(final WMSessionHandle shandle, final String procId, final String actId)
			throws EventAuditException {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List restoreProcessHistory(final WMSessionHandle shandle, final String procId) throws EventAuditException {
		return null;
	}

}
