package integration.mail;

import java.util.Set;

import org.cmdbuild.common.Builder;
import org.junit.rules.ExternalResource;

import com.google.common.collect.Sets;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class GreenMailServer extends ExternalResource {

	public static interface Hooks {

		void beforeStart();

	}

	private static final Hooks NULL_HOOKS = new Hooks() {

		@Override
		public void beforeStart() {
			// TODO Auto-generated method stub
		}

	};

	private static class User {

		public final String email;
		public final String login;
		public final String password;

		public User(final String email, final String password) {
			this(email, email, password);
		}

		public User(final String email, final String login, final String password) {
			this.email = email;
			this.login = login;
			this.password = password;
		}

	}

	public static class GreenMailServerBuilder implements Builder<GreenMailServer> {

		private ServerSetup[] configuration;
		private Hooks hooks;
		private final Set<User> users;

		private GreenMailServerBuilder() {
			configuration = ServerSetup.ALL;
			hooks = NULL_HOOKS;
			users = Sets.newHashSet();
		}

		@Override
		public GreenMailServer build() {
			return new GreenMailServer(this);
		}

		public GreenMailServerBuilder withConfiguration(final ServerSetup configuration) {
			this.configuration = new ServerSetup[] { configuration };
			return this;
		}

		public GreenMailServerBuilder withConfiguration(final ServerSetup... configuration) {
			this.configuration = configuration;
			return this;
		}

		public GreenMailServerBuilder withHooks(final Hooks hooks) {
			this.hooks = hooks;
			return this;
		}

		public GreenMailServerBuilder withUser(final String email, final String password) {
			this.users.add(new User(email, password));
			return this;
		}

		public GreenMailServerBuilder withUser(final String email, final String username, final String password) {
			this.users.add(new User(email, username, password));
			return this;
		}

	}

	public static GreenMailServerBuilder newInstance() {
		return new GreenMailServerBuilder();
	}

	/**
	 * Needed for make this tests always working on the CI server.
	 */
	private static final long TIMEOUT_NEEDED_FOR_GREENMAIL_TO_BE_FULLY_INITIALIZED = 1000;

	private final ServerSetup[] configuration;
	private final Hooks hooks;
	private final Iterable<User> users;

	private GreenMail greenMail;

	public GreenMailServer(final GreenMailServerBuilder builder) {
		this.configuration = builder.configuration;
		this.hooks = builder.hooks;
		this.users = builder.users;
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		hooks.beforeStart();
		greenMail = new GreenMail(configuration);
		greenMail.start();
		for (final User user : users) {
			greenMail.setUser(user.email, user.login, user.password);
		}
		Thread.sleep(TIMEOUT_NEEDED_FOR_GREENMAIL_TO_BE_FULLY_INITIALIZED);
	}

	@Override
	protected void after() {
		super.after();
		greenMail.stop();
	}

	public GreenMail getServer() {
		return greenMail;
	}

}
