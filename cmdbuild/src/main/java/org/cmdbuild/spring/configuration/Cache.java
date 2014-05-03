package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;
import static org.cmdbuild.spring.util.Constants.SOAP;

import java.util.Arrays;

import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.logic.cache.DefaultCachingLogic;
import org.cmdbuild.services.cache.CachingService;
import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.cmdbuild.services.cache.DefaultCachingService;
import org.cmdbuild.services.cache.wrappers.DatabaseDriverWrapper;
import org.cmdbuild.services.cache.wrappers.DmsServiceWrapper;
import org.cmdbuild.services.cache.wrappers.JSONDispatcherServiceWrapper;
import org.cmdbuild.services.cache.wrappers.TranslationServiceWrapper;
import org.cmdbuild.services.soap.security.SoapUserFetcher;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Cache {

	@Autowired
	private Data data;

	@Autowired
	private AbstractDBDriver driver;

	@Autowired
	private DmsService dmsService;

	@Autowired
	@Qualifier(SOAP)
	private SoapUserFetcher soapUserFetcher;

	@Bean
	public CachingService cachingService() {
		return new DefaultCachingService(Arrays.asList( //
				databaseDriverWrapper(), //
				dmsServiceWrapper(), //
				data.cachedLookupStore(), //
				translationServiceWrapper(), //
				jsonDispatcherServiceWrapper(), //
				soapUserFetcher));
	}

	@Bean
	protected Cacheable databaseDriverWrapper() {
		return new DatabaseDriverWrapper(driver);
	}

	@Bean
	protected Cacheable dmsServiceWrapper() {
		return new DmsServiceWrapper(dmsService);
	}

	@Bean
	protected Cacheable translationServiceWrapper() {
		return new TranslationServiceWrapper();
	}

	@Bean
	protected Cacheable jsonDispatcherServiceWrapper() {
		return new JSONDispatcherServiceWrapper();
	}

	@Bean
	@Scope(PROTOTYPE)
	// FIXME why prototype?
	public CachingLogic defaultCachingLogic() {
		return new DefaultCachingLogic(cachingService());
	}

}
