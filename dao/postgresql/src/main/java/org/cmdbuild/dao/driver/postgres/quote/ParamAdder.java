package org.cmdbuild.dao.driver.postgres.quote;

/*
 * TODO should not use this trick for add parameters to the part creator
 */
public interface ParamAdder {

	void add(Object value);

}
