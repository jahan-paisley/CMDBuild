package org.cmdbuild.shark.toolagent;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ManageCardToolAgent extends AbstractConditionalToolAgent {

	protected final Map<String, Object> getAttributeMap() {
		final Map<String, Object> attributes = new HashMap<String, Object>();
		if (isMeta()) {
			for (final Map.Entry<String, Object> entry : getInputParameterValues().entrySet()) {
				final String name = entry.getKey();
				if (isFixedAttributeName(name)) {
					continue;
				}
				attributes.put(name, entry.getValue());
			}
		} else {
			attributes.putAll(getAttributesForNonMetaInvoke());
		}
		return attributes;
	}

	private boolean isMeta() {
		return !notMetaToolNames().contains(getId());
	}

	/**
	 * Returns the names that have a special meaning, thus are not interpreted
	 * as meta-tool invocations.
	 * 
	 * @return application names not interpreted as meta-tool invocations
	 */
	protected List<String> notMetaToolNames() {
		return emptyList();
	}

	private boolean isFixedAttributeName(final String name) {
		return fixedMetaAttributeNames().contains(name);
	}

	/**
	 * Returns the attribute names that are part of the meta-tool "signature".
	 * 
	 * @return attribute names
	 */
	protected abstract List<String> fixedMetaAttributeNames();

	/**
	 * Returns the parameter map if the tool is not invoked as meta-tool.
	 * 
	 * @return parameter map
	 */
	protected Map<String, Object> getAttributesForNonMetaInvoke() {
		return emptyMap();
	}

}