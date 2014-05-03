package org.cmdbuild.workflow.xpdl;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.workflow.xpdl.XpdlDocument.StandardAndCustomTypes;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.ActivitySet;
import org.enhydra.jxpdl.elements.DataField;
import org.enhydra.jxpdl.elements.Participant;
import org.enhydra.jxpdl.elements.Transition;
import org.enhydra.jxpdl.elements.WorkflowProcess;

public class XpdlProcess implements XpdlActivityHolder, XpdlExtendedAttributesHolder {

	@Legacy("As in 1.x")
	private static final String BIND_TO_CLASS_XA = "cmdbuildBindToClass";

	private final XpdlDocument doc;
	final WorkflowProcess inner;

	private final XpdlActivities activities;
	private final XpdlExtendedAttributes extendedAttributes;
	private final XpdlApplications applications;

	XpdlProcess(final XpdlDocument doc, final WorkflowProcess workflowProcess) {
		Validate.notNull(doc);
		Validate.notNull(workflowProcess);
		this.doc = doc;
		this.inner = workflowProcess;
		this.activities = new XpdlProcessActivities(this);
		this.extendedAttributes = new XpdlProcessExtendedAttributes(this);
		this.applications = new XpdlProcessApplications(this);
	}

	public XpdlDocument getDocument() {
		return doc;
	}

	public String getId() {
		return inner.getId();
	}

	public void addField(final String dfId, final StandardAndCustomTypes type) {
		doc.turnReadWrite();
		final DataField df = doc.createDataField(dfId, type);
		inner.getDataFields().add(df);
	}

	/**
	 * Creates and adds a new activity set to a process.
	 * 
	 * @param process
	 *            id
	 * @param activity
	 *            set id
	 * @return the created activity set
	 */
	public XpdlActivitySet createActivitySet(final String activitySetId) {
		doc.turnReadWrite();
		final ActivitySet as = (ActivitySet) inner.getActivitySets().generateNewElement();
		as.setId(activitySetId);
		inner.getActivitySets().add(as);
		return new XpdlActivitySet(this, as);
	}

	public XpdlActivitySet findActivitySet(final String activitySetId) {
		final ActivitySet as = inner.getActivitySets().getActivitySet(activitySetId);
		if (as != null) {
			return new XpdlActivitySet(this, as);
		} else {
			return null;
		}
	}

	@Override
	public void addExtendedAttribute(final String key, final String value) {
		extendedAttributes.addExtendedAttribute(key, value);
	}

	@Override
	public String getFirstExtendedAttributeValue(final String key) {
		return extendedAttributes.getFirstExtendedAttributeValue(key);
	}

	@Override
	public XpdlActivity createActivity(final String activityId) {
		return activities.createActivity(activityId);
	}

	@Override
	public XpdlActivity getActivity(final String activityId) {
		return new XpdlActivity(this, inner.getActivity(activityId));
	}

	@Override
	public List<XpdlActivity> getStartActivities() {
		return activities.getStartActivities();
	}

	@Override
	public List<XpdlActivity> getManualStartActivitiesRecursive() {
		return activities.getManualStartActivitiesRecursive();
	}

	@Legacy("As in 1.x")
	public void setBindToClass(final String className) {
		extendedAttributes.addOrModifyExtendedAttribute(BIND_TO_CLASS_XA, className);
	}

	@Legacy("As in 1.x")
	public String getBindToClass() {
		return extendedAttributes.getFirstExtendedAttributeValue(BIND_TO_CLASS_XA);
	}

	public XpdlTransition createTransition(final XpdlActivity from, final XpdlActivity to) {
		doc.turnReadWrite();
		final Transition transition = (Transition) inner.getTransitions().generateNewElement();
		transition.setId(String.format("%s--%s", from.getId(), to.getId()));
		transition.setFrom(from.getId());
		transition.setTo(to.getId());
		transition.getCondition().setTypeNONE();

		inner.getTransitions().add(transition);

		return new XpdlTransition(this, transition);
	}

	public XpdlApplication createApplication(final String id) {
		return applications.createApplication(id);
	}

	public void addRoleParticipant(final String participantId) {
		doc.turnReadWrite();
		Participant p = (Participant) inner.getParticipants().generateNewElement();
		p.setId(participantId);
		// Default but better safe than sorry
		p.getParticipantType().setTypeROLE();
		inner.getParticipants().add(p);
	}

	public boolean hasRoleParticipant(final String participantId) {
		final Participant p = inner.getParticipants().getParticipant(participantId);
		return (p != null) && (XPDLConstants.PARTICIPANT_TYPE_ROLE.equals(p.getParticipantType().getType()))
				|| doc.hasRoleParticipant(participantId);
	}
}