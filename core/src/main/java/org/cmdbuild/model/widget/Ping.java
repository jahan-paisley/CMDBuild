package org.cmdbuild.model.widget;

import java.util.Map;

import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.utils.OS;

public class Ping extends AbstractCommandExecutionWidget {

	private static final String PING_COMMAND_TEMPLATE = OS.isWindows() ? "ping -n %d %s" : "ping -c %d %s";

	private String address;
	private int count;
	private Map<String, String> templates;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setCount(final int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setTemplates(final Map<String, String> templates) {
		this.templates = templates;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	@Override
	protected WidgetAction getActionCommand(final String action, final Map<String, Object> params,
			final Map<String, Object> dsVars) {
		if ("legacytr".equals(action)) {
			final String address = String.valueOf(params.get("address"));
			final String command = getPingCommandLine(address);
			return new ExecuteCommandAction(command);
		} else {
			return super.getActionCommand(action, params, dsVars);
		}
	}

	@Override
	protected String getCommandLine(final TemplateResolver tr) {
		final String resolvedAddress = tr.resolve(getAddress());
		return getPingCommandLine(resolvedAddress);
	}

	private String getPingCommandLine(final String address) {
		return String.format(PING_COMMAND_TEMPLATE, getCount(), address);
	}
}
