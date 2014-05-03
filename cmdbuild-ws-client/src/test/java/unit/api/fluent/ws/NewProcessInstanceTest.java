package unit.api.fluent.ws;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static utils.matchers.AttributeListMatcher.containsAttribute;

import java.util.Arrays;
import java.util.List;

import org.cmdbuild.api.fluent.NewProcessInstance;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;
import org.junit.Before;
import org.junit.Test;

public class NewProcessInstanceTest extends AbstractWsFluentApiTest {

	private static final List<WorkflowWidgetSubmission> EMPTY_WIDGET_SUBMISSION = Arrays
			.asList(new WorkflowWidgetSubmission[0]);
	private static final org.cmdbuild.services.soap.Workflow PROCESS_INSTANCE_INFO;

	static {
		PROCESS_INSTANCE_INFO = new org.cmdbuild.services.soap.Workflow();
		PROCESS_INSTANCE_INFO.setProcessid(CARD_ID);
		PROCESS_INSTANCE_INFO.setProcessinstanceid("XYZ");
	}

	private NewProcessInstance newProcessInstance;

	@Before
	public void createNewProcessInstance() throws Exception {
		newProcessInstance = api() //
				.newProcessInstance(CLASS_NAME) //
				.withDescription(DESCRIPTION_VALUE) //
				.with(ATTRIBUTE_1, ATTRIBUTE_1_VALUE) //
				.withAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void parametersPassedToProxyWhenNewProcessInstanceCreated() throws Exception {
		when(proxy().updateWorkflow(any(Card.class), anyBoolean(), any(List.class))).thenReturn(PROCESS_INSTANCE_INFO);

		newProcessInstance.start();

		verify(proxy()).updateWorkflow(cardCapturer(), anyBoolean(), eq(EMPTY_WIDGET_SUBMISSION));
		verifyNoMoreInteractions(proxy());

		final Card wsCard = capturedCard();
		assertThat(wsCard.getClassName(), equalTo(newProcessInstance.getClassName()));
		assertThat(wsCard.getAttributeList(), containsAttribute(DESCRIPTION_ATTRIBUTE, DESCRIPTION_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_1, ATTRIBUTE_1_VALUE));
		assertThat(wsCard.getAttributeList(), containsAttribute(ATTRIBUTE_2, ATTRIBUTE_2_VALUE));
		assertThat(wsCard.getAttributeList(), not(containsAttribute(MISSING_ATTRIBUTE)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void advancementIsHandledCorrectly() throws Exception {
		when(proxy().updateWorkflow(any(Card.class), anyBoolean(), any(List.class))).thenReturn(PROCESS_INSTANCE_INFO);

		newProcessInstance.startAndAdvance();

		verify(proxy()).updateWorkflow(any(Card.class), eq(true), any(List.class));
		verifyNoMoreInteractions(proxy());
	}

}
