package integration.widget;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.model.widget.AbstractCommandExecutionWidget.ExecuteCommandAction;
import org.cmdbuild.model.widget.Ping;
import org.junit.Test;

public class PingWidgetTest {

	private class PingTestDouble extends Ping {
		@Override
		public String getCommandLine(final TemplateResolver tr) {
			return super.getCommandLine(tr);
		}
	};

	private static final String LOCALHOST;
	private static final String LOCALIP;

	static {
		InetAddress local;
		try {
			local = InetAddress.getLocalHost();
		} catch (final UnknownHostException e) {
			local = null;
		}
		if (local != null) {
			LOCALHOST = local.getHostName();
			LOCALIP = local.getHostAddress();
		} else {
			LOCALHOST = "localhost";
			LOCALIP = "127.0.0.1";
		}
	}

	@Test
	public void pingsLocalNumericAddress() throws Exception {
		assertPingsAddress(LOCALIP);
	}

	@Test
	public void pingsLocalhost() throws Exception {
		assertPingsAddress(LOCALHOST);
	}

	// FIXME: very bad test and design
	@Test(timeout = 3000, expected = TimeoutException.class)
	public void actionThrowsOnTimeoutExpired() throws Exception {
		final TemplateResolver tr = mock(TemplateResolver.class);
		final PingTestDouble pingWidget = new PingTestDouble();
		pingWidget.setCount(10);
		when(tr.resolve(anyString())).thenReturn(LOCALIP);
		final String command = pingWidget.getCommandLine(tr);

		final ExecuteCommandAction action = new ExecuteCommandAction(command, 100L);

		action.execute();
	}

	@Test
	public void addressIsConsideredATemplate() throws Exception {
		final TemplateResolver tr = mock(TemplateResolver.class);
		final PingTestDouble pingWidget = new PingTestDouble();
		pingWidget.setAddress(LOCALHOST);

		pingWidget.getCommandLine(tr);

		verify(tr, only()).resolve(LOCALHOST);
	}

	/*
	 * Utils
	 */

	@SuppressWarnings("unchecked")
	private void assertPingsAddress(final String address) throws Exception {
		final String result = (String) newPingWidget(address, 1).executeAction(null, null, null);
		assertThat(result.toLowerCase(), allOf(containsString("byte"), containsString("ms"), containsString("ping")));
	}

	private Ping newPingWidget(final String address, final int count) {
		final Ping pingWidget = new Ping();
		pingWidget.setAddress(address);
		pingWidget.setCount(count);
		return pingWidget;
	}
}
