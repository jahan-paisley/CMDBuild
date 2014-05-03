package org.cmdbuild.services.store;

import java.util.Map;

import org.cmdbuild.model.dashboard.DashboardDefinition;

public interface DashboardStore {
	public Long create(DashboardDefinition dashboard);

	public void update(Long dashboardId, DashboardDefinition dashboard);

	public void delete(Long dashboardId);

	public DashboardDefinition read(Long dashboardId);

	public Map<Integer, DashboardDefinition> list();
}
