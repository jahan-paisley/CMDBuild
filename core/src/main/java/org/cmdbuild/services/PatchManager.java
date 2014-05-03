package org.cmdbuild.services;

import java.sql.SQLException;
import java.util.List;

public interface PatchManager {

	public interface Patch {

		String getVersion();

		String getDescription();

		String getFilePath();

		@Override
		String toString();

	}

	void reset();

	void applyPatchList() throws SQLException;

	List<Patch> getAvaiblePatch();

	boolean isUpdated();

	void createLastPatch();

}
