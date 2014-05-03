package org.cmdbuild.model.widget;

public class OpenAttachment extends Widget {

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

}
