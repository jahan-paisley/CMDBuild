package org.cmdbuild.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringIntegrationUtils implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	public static ApplicationContext applicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		SpringIntegrationUtils.applicationContext = applicationContext;
	}

}
