package org.cmdbuild.logic.bim.project;

import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;

import com.google.common.base.Function;

public class ConversionUtils {

	public static final Function<PersistenceProject, PersistenceProject> TO_MODIFIABLE_PERSISTENCE_PROJECT = new Function<PersistenceProject, PersistenceProject>() {
		@Override
		public PersistenceProject apply(final PersistenceProject input) {
			final DefaultPersistenceProject modifiableProject = new DefaultPersistenceProject();
			modifiableProject.setActive(input.isActive());
			modifiableProject.setDescription(input.getDescription());
			modifiableProject.setExportProjectId(input.getExportProjectId());
			modifiableProject.setLastCheckin(input.getLastCheckin());
			modifiableProject.setName(input.getName());
			modifiableProject.setProjectId(input.getProjectId());
			modifiableProject.setSynch(input.isSynch());
			modifiableProject.setCardBinding(input.getCardBinding());
			return modifiableProject;
		}
	};

}
