package org.cmdbuild.cmdbf;

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
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RegistrationServiceMetadata;

public interface ManagementDataRepository {

	public String getMdrId();

	public QueryResultType graphQuery(QueryType body) throws InvalidPropertyTypeFault, UnknownTemplateIDFault,
			ExpensiveQueryErrorFault, QueryErrorFault, XPathErrorFault, UnsupportedSelectorFault,
			UnsupportedConstraintFault;

	public RegisterResponseType register(RegisterRequestType body) throws UnsupportedRecordTypeFault,
			InvalidRecordFault, InvalidMDRFault, RegistrationErrorFault;

	public DeregisterResponseType deregister(DeregisterRequestType body) throws DeregistrationErrorFault,
			InvalidMDRFault;

	public QueryServiceMetadata getQueryServiceMetadata();

	public RegistrationServiceMetadata getRegistrationServiceMetadata();
}
