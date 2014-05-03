package utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.cmdbuild.shark.toolagent.GroovyToolAgent;

public class GroovyToolAgentWithTmpRepository extends GroovyToolAgent {

	@Override
	protected List<String> repositories() {
		final List<String> reporitories = new ArrayList<String>();
		reporitories.add(SystemUtils.JAVA_IO_TMPDIR);
		return reporitories;
	}

}