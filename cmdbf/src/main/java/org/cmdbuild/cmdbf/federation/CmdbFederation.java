/* TODO
 * 1) identity reconciliation
 * 2) FOREIGNKEY as relationships
 * 3) history as records
 */

package org.cmdbuild.cmdbf.federation;

import java.util.Collection;

import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.cmdbuild.config.CmdbfConfiguration;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.DeregistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidMDRFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidRecordFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.UnsupportedRecordTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ObjectFactory;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.PropertyValueOperatorsType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryCapabilities;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypeList;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypes;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RegistrationServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ServiceDescription;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.XPathType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CmdbFederation implements ManagementDataRepository {
	private final Collection<ManagementDataRepository> mdrCollection;
	private final CmdbfConfiguration cmdbfConfiguration;

	public CmdbFederation(final Collection<ManagementDataRepository> mdrCollection,
			final CmdbfConfiguration cmdbfConfiguration) {
		this.mdrCollection = mdrCollection;
		this.cmdbfConfiguration = cmdbfConfiguration;
	}

	@Override
	public String getMdrId() {
		return cmdbfConfiguration.getMdrId();
	}

	@Override
	public QueryResultType graphQuery(final QueryType body) throws InvalidPropertyTypeFault, UnknownTemplateIDFault,
			ExpensiveQueryErrorFault, QueryErrorFault, XPathErrorFault, UnsupportedSelectorFault,
			UnsupportedConstraintFault {
		return new FederationQueryResult(body, mdrCollection);
	}

	@Override
	public RegisterResponseType register(final RegisterRequestType body) throws UnsupportedRecordTypeFault,
			InvalidRecordFault, InvalidMDRFault, RegistrationErrorFault {
		final ManagementDataRepository mdr = Iterables.find(mdrCollection, new Predicate<ManagementDataRepository>() {
			@Override
			public boolean apply(final ManagementDataRepository input) {
				return input.getMdrId().equals(body.getMdrId());
			}
		});
		if (mdr != null) {
			return mdr.register(body);
		} else {
			throw new InvalidMDRFault(body.getMdrId());
		}
	}

	@Override
	public DeregisterResponseType deregister(final DeregisterRequestType body) throws DeregistrationErrorFault,
			InvalidMDRFault {
		final ManagementDataRepository mdr = Iterables.find(mdrCollection, new Predicate<ManagementDataRepository>() {
			@Override
			public boolean apply(final ManagementDataRepository input) {
				return input.getMdrId().equals(body.getMdrId());
			}
		});
		if (mdr != null) {
			return mdr.deregister(body);
		} else {
			throw new InvalidMDRFault(body.getMdrId());
		}

	}

	@Override
	public QueryServiceMetadata getQueryServiceMetadata() {
		final ObjectFactory factory = new ObjectFactory();
		final QueryServiceMetadata queryServiceMetadata = factory.createQueryServiceMetadata();
		queryServiceMetadata.setServiceDescription(getServiceDescription(factory));
		queryServiceMetadata.setRecordTypeList(getRecordTypesList(factory));
		queryServiceMetadata.setQueryCapabilities(getQueryCapabilities(factory));
		return queryServiceMetadata;
	}

	@Override
	public RegistrationServiceMetadata getRegistrationServiceMetadata() {
		final ObjectFactory factory = new ObjectFactory();
		final RegistrationServiceMetadata registrationServiceMetadata = factory.createRegistrationServiceMetadata();
		registrationServiceMetadata.setServiceDescription(getServiceDescription(factory));
		registrationServiceMetadata.setRecordTypeList(getRecordTypesList(factory));
		return registrationServiceMetadata;
	}

	private ServiceDescription getServiceDescription(final ObjectFactory factory) {
		final ServiceDescription serviceDescription = factory.createServiceDescription();
		serviceDescription.setMdrId(getMdrId());
		return serviceDescription;
	}

	private QueryCapabilities getQueryCapabilities(final ObjectFactory factory) {
		final QueryCapabilities queryCapabilities = factory.createQueryCapabilities();

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ContentSelectorType contentSelectorType = factory
				.createContentSelectorType();
		contentSelectorType.setPropertySelector(true);
		contentSelectorType.setRecordTypeSelector(true);
		queryCapabilities.setContentSelectorSupport(contentSelectorType);

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordConstraintType recordConstraintType = factory
				.createRecordConstraintType();
		recordConstraintType.setRecordTypeConstraint(true);
		recordConstraintType.setPropertyValueConstraint(true);
		final PropertyValueOperatorsType propertyValueOperatorsType = factory.createPropertyValueOperatorsType();
		propertyValueOperatorsType.setContains(true);
		propertyValueOperatorsType.setEqual(true);
		propertyValueOperatorsType.setGreater(true);
		propertyValueOperatorsType.setGreaterOrEqual(true);
		propertyValueOperatorsType.setIsNull(true);
		propertyValueOperatorsType.setLess(true);
		propertyValueOperatorsType.setLessOrEqual(true);
		propertyValueOperatorsType.setLike(true);
		recordConstraintType.setPropertyValueOperators(propertyValueOperatorsType);
		queryCapabilities.setRecordConstraintSupport(recordConstraintType);

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RelationshipTemplateType relationshipTemplateType = factory
				.createRelationshipTemplateType();
		relationshipTemplateType.setDepthLimit(true);
		relationshipTemplateType.setMinimumMaximum(true);
		queryCapabilities.setRelationshipTemplateSupport(relationshipTemplateType);

		final XPathType xPathType = factory.createXPathType();
		queryCapabilities.setXpathSupport(xPathType);
		return queryCapabilities;
	}

	private RecordTypeList getRecordTypesList(final ObjectFactory factory) {
		final RecordTypeList recordTypeList = factory.createRecordTypeList();
		for (final ManagementDataRepository mdr : mdrCollection) {
			final QueryServiceMetadata metadata = mdr.getQueryServiceMetadata();
			for (final RecordTypes recordTypes : metadata.getRecordTypeList().getRecordTypes()) {
				recordTypeList.getRecordTypes().add(recordTypes);
			}
		}
		return recordTypeList;
	}

}