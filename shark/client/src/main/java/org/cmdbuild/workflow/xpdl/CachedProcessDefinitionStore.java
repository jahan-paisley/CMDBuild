package org.cmdbuild.workflow.xpdl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSPackageDef;
import org.cmdbuild.workflow.service.WSPackageDefInfo;
import org.cmdbuild.workflow.service.WSProcessDefInfo;

public abstract class CachedProcessDefinitionStore implements ProcessDefinitionStore {

	private class PackageInfo {

		private final String id;
		private String currentVersion;
		private final Map<String, PackageVersionInfo> packageVersions = new HashMap<String, PackageVersionInfo>();

		public PackageInfo(final String id, final String currentVersion, final PackageVersionInfo currentVersionInfo) {
			this.id = id;
			setCurrentVersion(currentVersion, currentVersionInfo);
		}

		public String getId() {
			return id;
		}

		PackageVersionInfo getCurrentVersionInfo() {
			return packageVersions.get(currentVersion);
		}

		PackageVersionInfo getVersionInfo(final String version) {
			return packageVersions.get(version);
		}

		public void setCurrentVersion(final String currentVersion, final PackageVersionInfo currentVersionInfo) {
			this.currentVersion = currentVersion;
			this.packageVersions.put(currentVersion, currentVersionInfo);
		}

		public void addVersion(final String version, final PackageVersionInfo versionInfo) {
			this.packageVersions.put(version, versionInfo);
		}
	}

	protected interface PackageVersionInfo {
		byte[] getRawDefinition();

		ProcessInfo getProcess(String procDefId);

		Collection<ProcessInfo> getProcesses();
	}

	protected interface ProcessInfo {
		String getClassName();

		String getDefinitionId();

		List<CMActivity> getManualStartActivities();

		CMActivity getActivityById(String activityDefinitionId);
	}

	private class LazyClassNameToPackageInfoMap {

		boolean loaded = false;

		private volatile Map<String, PackageInfo> packageIdToPackageInfo = new HashMap<String, PackageInfo>();
		private volatile Map<String, PackageInfo> classNameToPackageInfo = new HashMap<String, PackageInfo>();
		private volatile Map<String, ProcessInfo> classNameToProcessInfo = new HashMap<String, ProcessInfo>();
		private volatile Map<String, ProcessInfo> definitionToProcessInfo = new HashMap<String, ProcessInfo>();

		public ProcessInfo getProcessInfoByClass(final String className) throws CMWorkflowException {
			checkLoaded();
			return classNameToProcessInfo.get(className);
		}

		public ProcessInfo getProcessInfoByDefinition(final String processDefinitionId) throws CMWorkflowException {
			checkLoaded();
			return definitionToProcessInfo.get(processDefinitionId);
		}

		/**
		 * Returns the package that contains the process bound to the requested
		 * class. If more than one, there is no guarantee that the returned one
		 * was the last one uploaded.
		 * 
		 * @param className
		 * @return package information
		 * @throws CMWorkflowException
		 */
		public PackageInfo getPackageInfoByClass(final String className) throws CMWorkflowException {
			checkLoaded();
			return classNameToPackageInfo.get(className);
		}

		public PackageInfo getPackageInfoById(final String pkgId) throws CMWorkflowException {
			checkLoaded();
			return packageIdToPackageInfo.get(pkgId);
		}

		private void checkLoaded() throws CMWorkflowException {
			if (!loaded) {
				synchronized (this) {
					if (!loaded) {
						fetchProcessDefinitions();
						loaded = true;
					}
				}
			}
		}

		private void fetchProcessDefinitions() throws CMWorkflowException {
			for (final WSPackageDef pkg : workflowService.downloadAllPackages()) {
				safeAddCurrentPackageVersion(pkg.getPackageId(), pkg.getPackageVersion(), pkg.getData());
			}
		}

		private void safeAddCurrentPackageVersion(final String pkgId, final String pkgVer, final byte[] pkgData)
				throws CMProcessDefinitionException {
			final PackageVersionInfo currentVersionInfo = createPackageVersionInfo(pkgData);
			PackageInfo packageInfo = packageIdToPackageInfo.get(pkgId);
			if (packageInfo == null) {
				packageInfo = new PackageInfo(pkgId, pkgVer, currentVersionInfo);
			} else {
				packageInfo.setCurrentVersion(pkgVer, currentVersionInfo);
			}

			updateMap(packageInfo);
		}

		public synchronized void addCurrentPackageVersion(final String pkgId, final String pkgVer, final byte[] pkgData)
				throws CMWorkflowException {
			checkLoaded();
			safeAddCurrentPackageVersion(pkgId, pkgVer, pkgData);
		}

		public synchronized PackageVersionInfo addPackageVersion(final String pkgId, final String pkgVer,
				final byte[] pkgData) throws CMWorkflowException {
			checkLoaded();
			final PackageVersionInfo versionInfo = createPackageVersionInfo(pkgData);
			final PackageInfo packageInfo = packageIdToPackageInfo.get(pkgId);
			if (packageInfo != null) {
				packageInfo.addVersion(pkgVer, versionInfo);
			}
			return versionInfo;
		}

		private void updateMap(final PackageInfo packageInfo) {
			packageIdToPackageInfo.put(packageInfo.getId(), packageInfo);
			for (final ProcessInfo procInfo : packageInfo.getCurrentVersionInfo().getProcesses()) {
				classNameToPackageInfo.put(procInfo.getClassName(), packageInfo);
				classNameToProcessInfo.put(procInfo.getClassName(), procInfo);
				definitionToProcessInfo.put(procInfo.getDefinitionId(), procInfo);
			}
		}
	}

	private final CMWorkflowService workflowService;

	public CachedProcessDefinitionStore(final CMWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	private final LazyClassNameToPackageInfoMap cache = new LazyClassNameToPackageInfoMap();

	@Override
	public List<CMActivity> getManualStartActivities(final String className) throws CMWorkflowException {
		final ProcessInfo pi = cache.getProcessInfoByClass(className);
		if (pi == null) {
			// it is null when the process was created but the XPDL is not yet
			// uploaded
			return Collections.<CMActivity> emptyList();
		} else {
			return pi.getManualStartActivities();
		}
	}

	@Override
	public CMActivity getActivity(final WSProcessDefInfo procDefInfo, final String activityDefinitionId)
			throws CMWorkflowException {
		return getProcessInfo(procDefInfo).getActivityById(activityDefinitionId);
	}

	/**
	 * Gets cached process info or downloads and caches it.
	 */
	public ProcessInfo getProcessInfo(final WSProcessDefInfo procDefInfo) throws CMWorkflowException {
		final PackageInfo pkgInfo = cache.getPackageInfoById(procDefInfo.getPackageId());
		PackageVersionInfo pkgVerInfo = pkgInfo.getVersionInfo(procDefInfo.getPackageVersion());
		if (pkgVerInfo == null) {
			pkgVerInfo = downloadAndCachePackageVersion(procDefInfo.getPackageVersion(), procDefInfo.getPackageId());
		}
		return pkgVerInfo.getProcess(procDefInfo.getProcessDefinitionId());
	}

	@Override
	public String[] getPackageVersions(final String className) throws CMWorkflowException {
		return workflowService.getPackageVersions(getPackageId(className));
	}

	@Override
	public final String getPackageId(final String className) throws CMWorkflowException {
		final PackageInfo pi = cache.getPackageInfoByClass(className);
		if (pi == null) {
			// it is null when the process was created but the XPDL is not yet
			// uploaded
			return null;
		} else {
			return pi.getId();
		}
	}

	@Override
	public String getProcessDefinitionId(final String className) throws CMWorkflowException {
		final ProcessInfo pi = cache.getProcessInfoByClass(className);
		if (pi == null) {
			// it is null when the process was created but the XPDL is not yet
			// uploaded
			return null;
		} else {
			return pi.getDefinitionId();
		}
	}

	@Override
	public String getProcessClassName(final String processDefinitionId) throws CMWorkflowException {
		final ProcessInfo pi = cache.getProcessInfoByDefinition(processDefinitionId);
		if (pi == null) {
			// it is null when the process was created but the XPDL is not yet
			// uploaded
			return null;
		} else {
			return pi.getClassName();
		}
	}

	@Override
	public byte[] downloadPackage(final String className, final String pkgVer) throws CMWorkflowException {
		final PackageVersionInfo pvi = cache.getPackageInfoByClass(className).getVersionInfo(pkgVer);
		if (pvi == null) {
			final String pkgId = getPackageId(className);
			return downloadAndCachePackageVersion(pkgVer, pkgId).getRawDefinition();
		} else {
			return pvi.getRawDefinition();
		}

	}

	private PackageVersionInfo downloadAndCachePackageVersion(final String pkgVer, final String pkgId)
			throws CMWorkflowException {
		final byte[] pkgData = workflowService.downloadPackage(pkgId, pkgVer);
		return cache.addPackageVersion(pkgId, pkgVer, pkgData);
	}

	@Override
	public synchronized void uploadPackage(final String className, final byte[] pkgData) throws CMWorkflowException {
		final String pkgId = getPackageId(className);
		final WSPackageDefInfo pkgInfo = workflowService.uploadPackage(pkgId, pkgData);
		cache.addCurrentPackageVersion(pkgInfo.getPackageId(), pkgInfo.getPackageVersion(), pkgData);
	}

	protected abstract PackageVersionInfo createPackageVersionInfo(byte[] pkgDef) throws CMProcessDefinitionException;
}
