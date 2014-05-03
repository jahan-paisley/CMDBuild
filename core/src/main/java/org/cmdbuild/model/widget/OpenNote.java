package org.cmdbuild.model.widget;

public class OpenNote extends Widget {

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

}
