package org.cmdbuild.report;

public interface ReportParameterVisitor {

	void accept(RPFake fake);

	void accept(RPLookup lookup);

	void accept(RPReference reference);

	void accept(RPSimple simple);

}
