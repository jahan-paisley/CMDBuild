package org.cmdbuild.notification;

import org.cmdbuild.exception.CMDBException;

public interface Notifier {

	void warn(CMDBException e);

}
