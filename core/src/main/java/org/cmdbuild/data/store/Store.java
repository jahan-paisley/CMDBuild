package org.cmdbuild.data.store;

import java.util.List;

import org.cmdbuild.dao.logging.LoggingSupport;
import org.slf4j.Logger;

public interface Store<T extends Storable> {

	Logger logger = LoggingSupport.logger;

	Storable create(T storable);

	T read(Storable storable);

	void update(T storable);

	void delete(Storable storable);

	List<T> list();

	List<T> list(Groupable groupable);

}
