package utils.async;

import org.hamcrest.Description;

public interface Probe {

	boolean isSatisfied();

	boolean isDone();

	void sample();

	void describeAcceptanceCriteriaTo(Description d);

	void describeFailureTo(Description d);

}
