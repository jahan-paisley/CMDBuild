package org.cmdbuild.bim.service.bimserver;

import java.util.Date;

import org.bimserver.interfaces.objects.SRevision;
import org.cmdbuild.bim.service.BimRevision;

public class BimserverRevision implements BimRevision {

	private final SRevision revision;

	protected BimserverRevision(final SRevision revision) {
		this.revision = revision;
	}

	@Override
	public String getIdentifier() {
		return Long.toString(getOid());
	}

	public long getOid() {
		return revision.getOid();
	}

	@Override
	public String getProjectId() {
		return Long.toString(revision.getProjectId());
	}

	@Override
	public Date getDate() {
		return revision.getDate();
	}

	@Override
	public String getUser() {
		return Long.toString(revision.getUserId());
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
