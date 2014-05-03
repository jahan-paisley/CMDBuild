package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.ROOT;

import javax.sql.DataSource;

import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.services.DefaultPatchManager;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.template.store.StoreTemplateRepository;
import org.cmdbuild.services.template.store.Template;
import org.cmdbuild.services.template.store.TemplateStorableConverter;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Other {

	@Autowired
	private Data data;

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier(ROOT)
	private FilesStore rootFilesStore;

	@Autowired
	private SystemDataAccessLogicBuilder systemDataAccessLogicBuilder;

	@Bean
	public PatchManager patchManager() {
		return new DefaultPatchManager( //
				dataSource, //
				data.systemDataView(), //
				systemDataAccessLogicBuilder, //
				data.dataDefinitionLogic(), //
				rootFilesStore);
	}

	@Bean
	public StoreTemplateRepository templateRepository() {
		return new StoreTemplateRepository(templateStore());
	}

	@Bean
	protected Store<Template> templateStore() {
		return DataViewStore.newInstance(data.systemDataView(), templateStorableConverter());
	}

	@Bean
	protected StorableConverter<Template> templateStorableConverter() {
		return new TemplateStorableConverter();
	}

	@Bean
	public MetadataStoreFactory metadataStoreFactory() {
		return new MetadataStoreFactory(data.systemDataView());
	}

}
