package org.cmdbuild.model.widget;

import org.cmdbuild.workflow.CMActivityWidget;

/**
 * This visitor should really visit {@link CMActivityWidget} class instead of
 * this. At the moment it's not possible because {@link CMActivityWidget} is
 * defined in the workflow module that doesn't know which
 * {@link CMActivityWidget} implementations have been created.
 * 
 * All in all it's not a so bad solution compared to other...
 */
public interface WidgetVisitor {

	interface WidgetVisitable {

		void accept(WidgetVisitor visitor);

	}

	void visit(Calendar calendar);

	void visit(CreateModifyCard createModifyCard);

	void visit(LinkCards linkCards);

	void visit(ManageEmail manageEmail);

	void visit(ManageRelation manageRelation);

	void visit(OpenAttachment openAttachment);

	void visit(OpenNote openNote);

	void visit(OpenReport openReport);

	void visit(Ping ping);

	void visit(WebService webService);

	void visit(PresetFromCard presetFromCard);

	void visit(Workflow workflow);

	void visit(NavigationTree navigationTree);

	void visit(Grid grid);
	
}
