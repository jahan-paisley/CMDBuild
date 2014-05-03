package org.cmdbuild.workflow;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.scripting.Evaluator;
import org.enhydra.shark.api.internal.scripting.ScriptingManager;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public abstract class ForwardingScriptingManager implements ScriptingManager {

	private final ScriptingManager delegate;

	protected ForwardingScriptingManager(final ScriptingManager delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		delegate.configure(cus);
	}

	@Override
	public Evaluator getEvaluator(final WMSessionHandle sessionHandle, final String name) throws Exception {
		return delegate.getEvaluator(sessionHandle, name);
	}

}
