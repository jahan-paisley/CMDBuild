package org.cmdbuild.logic;

import org.slf4j.Logger;

public interface Action {
	
	Logger logger = Logic.logger;

	public void execute();

}
