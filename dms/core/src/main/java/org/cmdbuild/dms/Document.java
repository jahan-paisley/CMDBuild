package org.cmdbuild.dms;

import java.util.List;

public interface Document {

	String getClassName();

	String getCardId();

	List<String> getPath();

}
