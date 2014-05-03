package org.cmdbuild.shark.toolagent;

import org.cmdbuild.shark.util.CmdbuildUtils;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.DefaultToolAgent;

public class CmdbuildLegacyDefaultToolAgent extends DefaultToolAgent {

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		CmdbuildUtils.getInstance().configure(cus);
	}

}
