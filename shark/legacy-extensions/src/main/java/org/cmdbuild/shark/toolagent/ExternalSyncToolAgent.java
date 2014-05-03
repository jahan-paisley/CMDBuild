package org.cmdbuild.shark.toolagent;

import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.CusSoapProxyBuilder;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

/**
 * Tool agent class for external sync, used by old CMDBuild connector only.
 * 
 * Parameters are get by position because we are not sure that has always been
 * called with the same parameter formal name.
 */
public class ExternalSyncToolAgent extends AbstractConditionalToolAgent {

	private Private proxy;

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		proxy = new CusSoapProxyBuilder(cus).build();
	}

	@Override
	protected void innerInvoke() throws Exception {
		setOutput(proxy.sync(xml()));
	}

	private String xml() {
		return (String) parameters[1].the_value;
	}

	private void setOutput(final String output) {
		parameters[2].the_value = output;
	}

}
