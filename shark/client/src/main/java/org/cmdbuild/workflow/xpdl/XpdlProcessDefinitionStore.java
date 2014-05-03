package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.service.CMWorkflowService;

public class XpdlProcessDefinitionStore extends CachedProcessDefinitionStore {

	private class XpdlPackageVersionInfo implements PackageVersionInfo {

		private final Map<String, ProcessInfo> processInfoById;
		private final byte[] raw;

		public XpdlPackageVersionInfo(final byte[] pkgDef) throws XpdlException {
			final XpdlDocument xpdl = new XpdlDocument(XpdlPackageFactory.readXpdl(pkgDef));
			this.processInfoById = createProcessInfoMap(xpdl);
			this.raw = pkgDef;
		}

		private Map<String, ProcessInfo> createProcessInfoMap(final XpdlDocument xpdl) {
			final Map<String, ProcessInfo> out = new HashMap<String, ProcessInfo>();
			for (final XpdlProcess xproc : xpdl.findAllProcesses()) {
				final String className = xproc.getBindToClass();
				if (className != null) {
					final ProcessInfo pi = new XpdlProcessInfo(xproc);
					out.put(pi.getDefinitionId(), pi);
				}
			}
			return out;
		}

		@Override
		public byte[] getRawDefinition() {
			return raw;
		}

		@Override
		public Collection<ProcessInfo> getProcesses() {
			return processInfoById.values();
		}

		@Override
		public ProcessInfo getProcess(final String procDefId) {
			return processInfoById.get(procDefId);
		}
	}

	private class XpdlProcessInfo implements ProcessInfo {
		private final XpdlProcess xproc;

		public XpdlProcessInfo(final XpdlProcess xproc) {
			this.xproc = xproc;
		}

		@Override
		public String getClassName() {
			return xproc.getBindToClass();
		}

		@Override
		public String getDefinitionId() {
			return xproc.getId();
		}

		@Override
		public List<CMActivity> getManualStartActivities() {
			final List<CMActivity> out = new ArrayList<CMActivity>();
			for (final XpdlActivity xact : xproc.getManualStartActivitiesRecursive()) {
				out.add(new XpdlActivityWrapper(xact, xpdlVariableFactory, xpdlWidgetFactory));
			}
			return out;
		}

		@Override
		public CMActivity getActivityById(final String activityDefinitionId) {
			final XpdlActivity xact = xproc.getActivity(activityDefinitionId);
			return new XpdlActivityWrapper(xact, xpdlVariableFactory, xpdlWidgetFactory);
		}
	}

	private final XpdlExtendedAttributeVariableFactory xpdlVariableFactory;
	private final XpdlExtendedAttributeWidgetFactory xpdlWidgetFactory;

	public XpdlProcessDefinitionStore(final CMWorkflowService workflowService,
			final XpdlExtendedAttributeVariableFactory xpdlvariablefactory,
			final XpdlExtendedAttributeWidgetFactory xpdlwidgetfactory) {
		super(workflowService);
		this.xpdlVariableFactory = xpdlvariablefactory;
		this.xpdlWidgetFactory = xpdlwidgetfactory;
	}

	@Override
	protected PackageVersionInfo createPackageVersionInfo(final byte[] pkgDef) throws XpdlException {
		return new XpdlPackageVersionInfo(pkgDef);
	}

}