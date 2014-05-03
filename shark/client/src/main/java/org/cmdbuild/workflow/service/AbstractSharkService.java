package org.cmdbuild.workflow.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.transaction.NotSupportedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.event.NullUpdateOperationListener;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttributeIterator;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.PackageAdministration;
import org.enhydra.shark.api.client.wfservice.SharkInterface;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.client.utilities.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.MiscUtilities;

/**
 * Base class for Shark implementations.
 * 
 * Public methods can't call themselves until the {@link TransactedSharkService}
 * is modified not to start a new transaction if one has been already started.
 * Otherwise it will raise a {@link NotSupportedException}
 * "Nested transactions not supported".
 */
public abstract class AbstractSharkService implements CMWorkflowService {

	/**
	 * Listener for update operations.
	 * 
	 * It's terse but it works for now.
	 */
	public interface UpdateOperationListener {

		void processInstanceStarted(int sessionId) throws CMWorkflowException;

		void processInstanceAborted(int sessionId) throws CMWorkflowException;

		void processInstanceSuspended(int sessionId) throws CMWorkflowException;

		void processInstanceResumed(int sessionId) throws CMWorkflowException;

		void activityInstanceAborted(int sessionId) throws CMWorkflowException;

		void activityInstanceAdvanced(int sessionId) throws CMWorkflowException;

		void abortedOperation(int sessionId) throws CMWorkflowException;

	}

	protected static final String DEFAULT_ENGINE_NAME = StringUtils.EMPTY;
	protected static final String DEFAULT_SCOPE = StringUtils.EMPTY;
	protected static final String LAST_VERSION = StringUtils.EMPTY;

	private static final UpdateOperationListener NULL_UPDATE_OPERATION_LISTENER = new NullUpdateOperationListener();

	/**
	 * It should be accessed through the {@see shark()} method only.
	 */
	private volatile SharkInterface _shark;

	/**
	 * It should be accessed through the {@see wapi()} method only.
	 */
	private volatile WAPI _wapi;

	private UpdateOperationListener updateOperationListener;

	protected AbstractSharkService(final Properties props) {
		configureSharkInterfaceWrapper(props);
		updateOperationListener = NULL_UPDATE_OPERATION_LISTENER;
	}

	protected final void reconfigure(final Properties props) {
		synchronized (this) {
			configureSharkInterfaceWrapper(props);
			SharkInterfaceWrapper.killShark();
			_shark = null;
			_wapi = null;
		}
	}

	private void configureSharkInterfaceWrapper(final Properties props) {
		try {
			SharkInterfaceWrapper.setProperties(props, true);
		} catch (final RuntimeException e) {
			// Otherwise it ignores even unchecked exceptions
			throw e;
		} catch (final Exception e) {
			// Can never happen with this configuration! Shark APIs love to
			// throw java.lang.Exception even when it can't. Take a look at
			// SharkInterfaceWrapper.setProperty(...) and have a good laugh!
		}
	}

	public void setUpdateOperationListener(final UpdateOperationListener updateOperationListener) {
		Validate.notNull(updateOperationListener, "what a mess");
		this.updateOperationListener = updateOperationListener;
	}

	protected WAPI wapi() throws Exception {
		if (_wapi == null) {
			synchronized (this) {
				if (_wapi == null) {
					_wapi = shark().getWAPIConnection();
					configureWAPI(_wapi);
				}
			}
		}
		return _wapi;
	}

	protected SharkInterface shark() throws Exception {
		if (_shark == null) {
			synchronized (this) {
				if (_shark == null) {
					_shark = SharkInterfaceWrapper.getShark();
				}
			}
		}
		return _shark;
	}

	protected abstract WMSessionHandle handle();

	/**
	 * It can be overridden to add something to the WAPI interface ({@see
	 * RemoteSharkService}).
	 */
	protected void configureWAPI(final WAPI wapi) {
	}

	abstract protected WMConnectInfo getConnectionInfo();

	@Override
	public String[] getPackageVersions(final String pkgId) throws CMWorkflowException {
		try {
			return shark().getPackageAdministration().getPackageVersions(handle(), pkgId);
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public WSPackageDefInfo uploadPackage(final String pkgId, final byte[] pkgDefData) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			final PackageAdministration pa = shark().getPackageAdministration();
			final WMEntity uploadedPackage;
			if (pkgId == null || pa.getPackageVersions(handle, pkgId).length == 0) {
				uploadedPackage = pa.uploadPackage(handle, pkgDefData);
			} else {
				uploadedPackage = pa.updatePackage(handle, pkgId, pkgDefData);
			}
			return newWSPackageDefInfo(uploadedPackage.getPkgId(), uploadedPackage.getPkgVer());
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	private WSPackageDefInfo newWSPackageDefInfo(final String id, final String version) {
		return new WSPackageDefInfo() {

			@Override
			public String getPackageId() {
				return id;
			}

			@Override
			public String getPackageVersion() {
				return version;
			}

		};
	}

	@Override
	public byte[] downloadPackage(final String pkgId, final String pkgVer) throws CMWorkflowException {
		try {
			return shark().getPackageAdministration().getPackageContent(handle(), pkgId, pkgVer);
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public WSPackageDef[] downloadAllPackages() throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			final PackageAdministration pa = shark().getPackageAdministration();
			final String[] pkgIds = pa.getOpenedPackageIds(handle);
			final WSPackageDef[] out = new WSPackageDef[pkgIds.length];
			for (int i = 0; i < pkgIds.length; ++i) {
				final String id = pkgIds[i];
				final String version = pa.getCurrentPackageVersion(handle, id);
				final byte[] data = pa.getPackageContent(handle, id, version);
				out[i] = newWSPackageDef(id, version, data);
			}
			return out;
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	private WSPackageDef newWSPackageDef(final String id, final String version, final byte[] data) {
		return new WSPackageDef() {

			@Override
			public String getPackageId() {
				return id;
			}

			@Override
			public String getPackageVersion() {
				return version;
			}

			@Override
			public byte[] getData() {
				return data;
			}

		};
	}

	@Override
	public final WSProcessInstInfo startProcess(final String pkgId, final String procDefId) throws CMWorkflowException {
		return startProcess(pkgId, procDefId, Collections.<String, Object> emptyMap());
	}

	@Override
	public WSProcessInstInfo startProcess(final String pkgId, final String procDefId, final Map<String, ?> variables)
			throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			final String uniqueProcDefId = shark().getXPDLBrowser().getUniqueProcessDefinitionName(handle, pkgId,
					LAST_VERSION, procDefId);
			final String procInstId = wapi().createProcessInstance(handle, uniqueProcDefId, null);
			setProcessInstanceVariables(handle, procInstId, variables);
			final String newProcInstId = wapi().startProcess(handle, procInstId);
			updateOperationListener.processInstanceStarted(handle.getId());
			return newWSProcessInstInfo(uniqueProcDefId, newProcInstId);
		} catch (final Exception e) {
			updateOperationListener.abortedOperation(handle.getId());
			throw new CMWorkflowException(e);
		}
	}

	private WSProcessInstInfo newWSProcessInstInfo(final String uniqueProcDefId, final String procInstId) {
		return new WSProcessInstInfo() {

			@Override
			public String getPackageId() {
				return MiscUtilities.getProcessMgrPkgId(uniqueProcDefId);
			}

			@Override
			public String getPackageVersion() {
				return MiscUtilities.getProcessMgrVersion(uniqueProcDefId);
			}

			@Override
			public String getProcessDefinitionId() {
				return MiscUtilities.getProcessMgrProcDefId(uniqueProcDefId);
			}

			@Override
			public String getProcessInstanceId() {
				return procInstId;
			}

			@Override
			public WSProcessInstanceState getStatus() {
				return WSProcessInstanceState.OPEN; // Guessed
			}

		};
	}

	@Override
	public WSProcessInstInfo[] listOpenProcessInstances(final String procDefId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			final WMProcessInstance[] pis = wapi().listProcessInstances(handle,
					openProcessInstances(handle, procDefId), false).getArray();
			final WSProcessInstInfo[] out = new WSProcessInstInfo[pis.length];
			for (int i = 0; i < pis.length; ++i) {
				out[i] = WSProcessInstInfoImpl.newInstance(pis[i]);
			}
			return out;
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	private WMFilter openProcessInstances(final WMSessionHandle handle, final String procDefId) throws Exception {
		final ProcessFilterBuilder fb = shark().getProcessFilterBuilder();
		return fb.and(handle, fb.addProcessDefIdEquals(handle, procDefId),
				fb.addStateStartsWith(handle, SharkConstants.STATEPREFIX_OPEN));
	}

	@Override
	public WSProcessInstInfo getProcessInstance(final String procInstId) throws CMWorkflowException {
		try {
			final WMProcessInstance pi = wapi().getProcessInstance(handle(), procInstId);
			return WSProcessInstInfoImpl.newInstance(pi);
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public Map<String, Object> getProcessInstanceVariables(final String procInstId) throws CMWorkflowException {
		try {
			final Map<String, Object> variables = new HashMap<String, Object>();
			final WMAttributeIterator iterator = wapi()
					.listProcessInstanceAttributes(handle(), procInstId, null, false);
			for (final WMAttribute attribute : iterator.getArray()) {
				final String name = attribute.getName();
				final Object value = attribute.getValue();
				variables.put(name, value);
			}
			return variables;
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public void setProcessInstanceVariables(final String procInstId, final Map<String, ?> variables)
			throws CMWorkflowException {
		try {
			setProcessInstanceVariables(handle(), procInstId, variables);
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	private void setProcessInstanceVariables(final WMSessionHandle handle, final String procInstId,
			final Map<String, ?> variables) throws Exception {
		for (final String name : variables.keySet()) {
			final Object sharkValue = variables.get(name);
			wapi().assignProcessInstanceAttribute(handle, procInstId, name, sharkValue);
		}
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcessInstance(final String procInstId)
			throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			final WMFilter filter = openActivitiesForProcessInstance(handle, procInstId);
			final WMActivityInstance[] ais = wapi().listActivityInstances(handle, filter, false).getArray();
			final WSActivityInstInfo[] out = new WSActivityInstInfo[ais.length];
			for (int i = 0; i < ais.length; ++i) {
				out[i] = WSActivityInstInfoImpl.newInstance(ais[i]);
			}
			return out;
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	private WMFilter openActivitiesForProcessInstance(final WMSessionHandle handle, final String procInstId)
			throws Exception {
		final ActivityFilterBuilder fb = shark().getActivityFilterBuilder();
		return fb.and(handle, fb.addProcessIdEquals(handle, procInstId),
				fb.addStateStartsWith(handle, SharkConstants.STATEPREFIX_OPEN));
	}

	@Override
	public WSActivityInstInfo[] findOpenActivitiesForProcess(final String procDefId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			final WMFilter filter = openActivitiesForProcess(handle, procDefId);
			final WMActivityInstance[] ais = wapi().listActivityInstances(handle, filter, false).getArray();
			final WSActivityInstInfo[] out = new WSActivityInstInfo[ais.length];
			for (int i = 0; i < ais.length; ++i) {
				out[i] = WSActivityInstInfoImpl.newInstance(ais[i]);
			}
			return out;
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

	private WMFilter openActivitiesForProcess(final WMSessionHandle handle, final String procDefId) throws Exception {
		final ActivityFilterBuilder fb = shark().getActivityFilterBuilder();
		return fb.and(handle, fb.addProcessDefIdEquals(handle, procDefId),
				fb.addStateStartsWith(handle, SharkConstants.STATEPREFIX_OPEN));
	}

	@Override
	public void abortActivityInstance(final String procInstId, final String actInstId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			// From Shark's FAQ,
			// "terminate [...] tries to follow the next activity(s), [...] abort [...] doesn't."
			wapi().changeActivityInstanceState(handle(), procInstId, actInstId, WMActivityInstanceState.CLOSED_ABORTED);
			updateOperationListener.activityInstanceAborted(handle.getId());
		} catch (final Exception e) {
			updateOperationListener.abortedOperation(handle.getId());
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public void advanceActivityInstance(final String procInstId, final String actInstId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			wapi().changeActivityInstanceState(handle, procInstId, actInstId, WMActivityInstanceState.OPEN_RUNNING);
		} catch (final Exception e) {
			// Ignore: it might be open-running already...
		}
		try {
			wapi().changeActivityInstanceState(handle, procInstId, actInstId, WMActivityInstanceState.CLOSED_COMPLETED);
			updateOperationListener.activityInstanceAdvanced(handle.getId());
		} catch (final Exception e) {
			updateOperationListener.abortedOperation(handle.getId());
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public void abortProcessInstance(final String procInstId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			wapi().changeProcessInstanceState(handle, procInstId, WMProcessInstanceState.CLOSED_ABORTED);
			updateOperationListener.processInstanceAborted(handle.getId());
		} catch (final Exception e) {
			updateOperationListener.abortedOperation(handle.getId());
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public void suspendProcessInstance(final String procInstId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			wapi().changeProcessInstanceState(handle, procInstId, WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED);
			updateOperationListener.processInstanceSuspended(handle.getId());
		} catch (final Exception e) {
			updateOperationListener.abortedOperation(handle.getId());
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public void resumeProcessInstance(final String procInstId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			wapi().changeProcessInstanceState(handle, procInstId, WMProcessInstanceState.OPEN_RUNNING);
			updateOperationListener.processInstanceResumed(handle.getId());
		} catch (final Exception e) {
			updateOperationListener.abortedOperation(handle.getId());
			throw new CMWorkflowException(e);
		}
	}

	@Override
	public void deleteProcessInstance(final String procInstId) throws CMWorkflowException {
		final WMSessionHandle handle = handle();
		try {
			final ProcessFilterBuilder fb = shark().getProcessFilterBuilder();
			final WMFilter filter = fb.addIdEquals(handle, procInstId);
			shark().getExecutionAdministration().deleteProcessesWithFiltering(handle, filter);
		} catch (final Exception e) {
			throw new CMWorkflowException(e);
		}
	}

}
