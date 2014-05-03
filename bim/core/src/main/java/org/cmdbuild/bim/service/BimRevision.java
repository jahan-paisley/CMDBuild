package org.cmdbuild.bim.service;

import java.util.Date;

public interface BimRevision {

	String getIdentifier();

	String getProjectId();

	Date getDate();

	String getUser();
	
	boolean isValid();
	
	public static BimRevision NULL_REVISION = new BimRevision() {
		
		@Override
		public String getUser() {
			return null;
		}
		
		@Override
		public String getProjectId() {
			return null;
		}
		
		@Override
		public String getIdentifier() {
			return null;
		}
		
		@Override
		public Date getDate() {
			return null;
		}

		@Override
		public boolean isValid() {
			return false;
		}
	};

}
