package org.cmdbuild.services.bim.connector;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;

public abstract class DefaultMapperRules implements MapperRules {

	@Override
	public abstract CMCard fetchCardWithKey(String key, String className,
			CMDataView dataView);

	@Override
	public abstract Long findIdFromKey(String value, String className,
			CMDataView dataView);

}
