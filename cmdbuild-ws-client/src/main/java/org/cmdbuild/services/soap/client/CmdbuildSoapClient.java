package org.cmdbuild.services.soap.client;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.cmdbuild.common.Builder;

public class CmdbuildSoapClient<T> implements SoapClient<T> {

	public static enum PasswordType {

		NONE {
			@Override
			public String toString() {
				return WSConstants.PW_NONE;
			}
		},

		TEXT {
			@Override
			public String toString() {
				return WSConstants.PW_TEXT;
			}
		},

		DIGEST {
			@Override
			public String toString() {
				return WSConstants.PW_DIGEST;
			}
		};

		@Override
		public abstract String toString();

	}

	public static class SoapClientBuilder<T> implements Builder<CmdbuildSoapClient<T>> {

		private Class<T> proxyClass;
		private String url;
		private PasswordType passwordType;
		private String username;
		private String password;

		public SoapClientBuilder<T> forClass(final Class<T> proxyClass) {
			this.proxyClass = proxyClass;
			return this;
		}

		public SoapClientBuilder<T> withUrl(final String url) {
			this.url = url;
			return this;
		}

		public SoapClientBuilder<T> withPasswordType(final PasswordType passwordType) {
			this.passwordType = passwordType;
			return this;
		}

		public SoapClientBuilder<T> withUsername(final String username) {
			this.username = username;
			return this;
		}

		public SoapClientBuilder<T> withPassword(final String password) {
			this.password = password;
			return this;
		}

		public CmdbuildSoapClient<T> build() {
			Validate.notNull(proxyClass, "null proxy class");
			Validate.isTrue(isNotBlank(url), format("invalid url '%s'", url));
			passwordType = (passwordType == null) ? PasswordType.NONE : passwordType;
			if (passwordType != PasswordType.NONE) {
				Validate.isTrue(isNotBlank(username), format("invalid username '%s'", username));
				Validate.isTrue(isNotBlank(password), format("invalid password '%s'", password));
			}
			return new CmdbuildSoapClient<T>(this);
		}
	}

	private final Class<T> proxyClass;
	private final String url;
	private final PasswordType passwordType;
	private final String username;
	private final String password;
	private T proxy;

	private CmdbuildSoapClient(final SoapClientBuilder<T> builder) {
		this.proxyClass = builder.proxyClass;
		this.url = builder.url;
		this.passwordType = builder.passwordType;
		this.username = builder.username;
		this.password = builder.password;
	}

	public Class<T> getProxyClass() {
		return proxyClass;
	}

	public String getUrl() {
		return url;
	}

	public PasswordType getPasswordType() {
		return passwordType;
	}

	public String getUsername() {
		return username;
	}

	public T getProxy() {
		if (proxy == null) {
			proxy = createProxy();
		}
		return proxy;
	}

	@SuppressWarnings("unchecked")
	private T createProxy() {
		final JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
		proxyFactory.setServiceClass(proxyClass);
		proxyFactory.setAddress(url);
		final Object proxy = proxyFactory.create();

		final Map<String, Object> outProps = new HashMap<String, Object>();
		final String passwordType = this.passwordType.toString();
		if (!WSConstants.PW_NONE.equals(passwordType)) {
			outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
			outProps.put(WSHandlerConstants.PASSWORD_TYPE, passwordType);
			outProps.put(WSHandlerConstants.USER, username);
			outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new ClientPasswordCallback(username, password));
		}

		final Client client = ClientProxy.getClient(proxy);
		final Endpoint cxfEndpoint = client.getEndpoint();
		cxfEndpoint.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));

		return (T) proxy;
	}

	private final class ClientPasswordCallback implements CallbackHandler {

		private final String password;
		private final String username;

		public ClientPasswordCallback(final String username, final String password) {
			this.username = username;
			this.password = password;
		}

		public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			final WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
			if (username.equals(pc.getIdentifier())) {
				pc.setPassword(password);
			}
		}
	}

	public static <T> SoapClientBuilder<T> aSoapClient() {
		return new SoapClientBuilder<T>();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this) //
				.append("proxy class", proxyClass) //
				.append("url", url) //
				.append("password type", passwordType.name()) //
				.append("username", username) //
				.append("password", password) //
				.toString();
	}

}
