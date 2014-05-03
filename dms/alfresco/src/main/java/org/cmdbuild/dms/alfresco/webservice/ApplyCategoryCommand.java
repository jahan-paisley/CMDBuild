package org.cmdbuild.dms.alfresco.webservice;

import org.alfresco.webservice.classification.AppliedCategory;
import org.alfresco.webservice.classification.ClassificationServiceSoapBindingStub;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

class ApplyCategoryCommand extends AbstractSearchCommand<Boolean> {

	private Reference category;
	private String uuid;

	public ApplyCategoryCommand() {
		setResult(false);
	}

	public void setCategory(final Reference category) {
		this.category = category;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	@Override
	public void execute() {
		Validate.notNull(category, "null category");
		Validate.isTrue(StringUtils.isNotBlank(uuid), "invalid uuid '%s'", uuid);
		try {
			final AppliedCategory appliedCategory = new AppliedCategory();
			appliedCategory.setClassification("{http://www.alfresco.org/model/content/1.0}generalclassifiable");
			appliedCategory.setCategories(new Reference[] { category });

			final Reference reference = new Reference(STORE, uuid, null);

			final Predicate predicate = new Predicate();
			predicate.setStore(STORE);
			predicate.setNodes(new Reference[] { reference });

			final ClassificationServiceSoapBindingStub classificationService = WebServiceFactory
					.getClassificationService();
			classificationService.setCategories(predicate, new AppliedCategory[] { appliedCategory });

			setResult(true);
		} catch (final Exception e) {
			logger.error("error applying category", e);
			setResult(false);
		}
	}

	@Override
	public boolean isSuccessfull() {
		return getResult();
	}

}
