package utils;

import org.cmdbuild.workflow.SimpleEventManager.ActivityInstance;
import org.cmdbuild.workflow.SimpleEventManager.ProcessInstance;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class EventManagerMatchers {

	public static Matcher<ActivityInstance> isActivity(final XpdlActivity activity) {
		return hasActivityDefinitionId(activity.getId());
	}

	public static Matcher<ActivityInstance> hasActivityDefinitionId(final String activityDefinitionId) {
		return new TypeSafeMatcher<ActivityInstance>() {

			private ActivityInstance lastMatch;

			@Override
			public void describeTo(final Description arg0) {
				String.format("expected definition id %s but got %s", activityDefinitionId,
						lastMatch.getActivityDefinitionId());
			}

			@Override
			public boolean matchesSafely(final ActivityInstance activityInstance) {
				lastMatch = activityInstance;
				return activityDefinitionId.equals(activityInstance.getActivityDefinitionId());
			}

		};
	}

	public static Matcher<ProcessInstance> isProcess(final XpdlProcess process) {
		return hasProcessDefinitionId(process.getId());
	}

	public static Matcher<ProcessInstance> hasProcessDefinitionId(final String processDefinitionId) {
		return new TypeSafeMatcher<ProcessInstance>() {

			private ProcessInstance lastMatch;

			@Override
			public void describeTo(final Description arg0) {
				String.format("expected definition id %s but got %s", processDefinitionId,
						lastMatch.getProcessDefinitionId());
			}

			@Override
			public boolean matchesSafely(final ProcessInstance processInstance) {
				lastMatch = processInstance;
				return processDefinitionId.equals(processInstance.getProcessDefinitionId());
			}

		};
	}

}
