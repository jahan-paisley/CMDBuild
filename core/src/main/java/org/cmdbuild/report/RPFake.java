package org.cmdbuild.report;

import net.sf.jasperreports.engine.design.JRDesignParameter;

public class RPFake extends ReportParameter {

	public RPFake(final String name) {
		final JRDesignParameter jrParameter = new JRDesignParameter();
		jrParameter.setName(name);
		jrParameter.setDescription(name);
		setJrParameter(jrParameter);
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	@Override
	public boolean isRequired() {
		return false;
	}

}