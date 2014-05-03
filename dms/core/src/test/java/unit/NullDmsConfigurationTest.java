package unit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.exception.MissingConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NullDmsConfigurationTest {

	private final Object object;
	private final Method method;

	@Parameters
	public static Collection<Object[]> data() {
		final DmsConfiguration configuration = DmsConfiguration.NullDmsConfiguration.newInstance();
		final Collection<Object[]> parameters = new ArrayList<Object[]>();
		final Class<?> dmsPropertiesClass = DmsConfiguration.class;
		final Method[] methods = dmsPropertiesClass.getMethods();
		for (final Method method : methods) {
			parameters.add(new Object[] { configuration, method });
		}
		return parameters;
	}

	public NullDmsConfigurationTest(final Object object, final Method method) {
		this.object = object;
		this.method = method;
	}

	@Test(expected = MissingConfigurationException.class)
	public void everyMethodCalledMustThowInvocationTargetExceptionBecauseMissingPropertiesException() throws Throwable {
		try {
			final Class<?>[] parameterTypes = method.getParameterTypes();
			final Object[] parameters = new Object[parameterTypes.length];
			method.invoke(object, parameters);
		} catch (final InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
