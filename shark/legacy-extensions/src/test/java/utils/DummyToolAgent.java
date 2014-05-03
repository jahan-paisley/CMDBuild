package utils;

import static org.mockito.Mockito.mock;

import org.cmdbuild.shark.toolagent.AbstractConditionalToolAgent;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;

public class DummyToolAgent extends AbstractConditionalToolAgent {

	public static interface DummyToolAgentListener {

		void invoked();

		void startingStatus(long status);

	}

	public static final DummyToolAgentListener listener = mock(DummyToolAgentListener.class);
	public static long _status;
	public static boolean throwException = false;
	public static final ConditionEvaluator conditionEvaluator = mock(ConditionEvaluator.class);

	public static void init() {
		throwException = false;
	}

	public DummyToolAgent() {
		super(conditionEvaluator);
	}

	@Override
	protected void innerInvoke() throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy,
			ToolAgentGeneralException {
		listener.invoked();
		listener.startingStatus(status);
		_status = status;
		if (throwException) {
			throw new ToolAgentGeneralException("just for test");
		}
	}

	@Override
	protected void setStatus(final long status) {
		super.setStatus(status);
		_status = status;
	}

}
