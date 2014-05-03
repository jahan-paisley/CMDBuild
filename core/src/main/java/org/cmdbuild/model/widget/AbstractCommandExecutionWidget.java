package org.cmdbuild.model.widget;

import static org.cmdbuild.services.template.engine.EngineNames.ALL_DATA_SOURCES;
import static org.cmdbuild.services.template.engine.EngineNames.ALL_PARAMETERS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.common.template.engine.Engines;

public abstract class AbstractCommandExecutionWidget extends Widget {

	public static class ExecuteCommandAction implements WidgetAction {

		private static final long DEAFULT_TIMEOUT_MS = 30000L;

		private final String command;
		private final long timeout;

		public ExecuteCommandAction(final String command) {
			this(command, DEAFULT_TIMEOUT_MS);
		}

		public ExecuteCommandAction(final String command, final long timeout) {
			this.command = command;
			this.timeout = timeout;
		}

		@Override
		public String execute() throws Exception {
			final ExecutorService execService = Executors.newSingleThreadExecutor();
			final Future<String> future = execService.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					final Process proc = Runtime.getRuntime().exec(command);
					proc.waitFor();
					return stdOutAsString(proc);
				}
			});
			try {
				return future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (final ExecutionException e) {
				throw e;
			} catch (final TimeoutException e) {
				execService.shutdownNow();
				throw e;
			}
		}

		private String stdOutAsString(final Process proc) throws IOException {
			final InputStream is = proc.getInputStream();
			final InputStreamReader isr = new InputStreamReader(is);
			final BufferedReader br = new BufferedReader(isr);
			final StringBuilder buffer = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				buffer.append(line).append("\n");
			}
			return buffer.toString();
		}

	};

	@Override
	protected WidgetAction getActionCommand(final String action, final Map<String, Object> params,
			final Map<String, Object> dsVars) {
		final TemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(Engines.map(params), ALL_PARAMETERS) //
				.withEngine(Engines.map(dsVars), ALL_DATA_SOURCES) //
				.build();
		final String command = getCommandLine(tr);
		return new ExecuteCommandAction(command);
	}

	protected abstract String getCommandLine(TemplateResolver tr);

}
