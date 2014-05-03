package org.cmdbuild.services.soap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryPortType;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

@WebService(targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/query", endpointInterface = "org.dmtf.schemas.cmdbf._1.tns.query.QueryPortType")
public class CMDBfQueryImpl extends AbstractWebservice implements QueryPortType {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);
		this.applicationContext = applicationContext;
	}

	public ManagementDataRepository getMdr() {
		return applicationContext.getBean(ManagementDataRepository.class);
	}

	@Override
	@WebResult(name = "queryResult", targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/serviceData", partName = "body")
	@WebMethod(operationName = "GraphQuery")
	public QueryResultType graphQuery(
			@WebParam(partName = "body", name = "query", targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/serviceData") final QueryType body)
			throws InvalidPropertyTypeFault, UnknownTemplateIDFault, ExpensiveQueryErrorFault, QueryErrorFault,
			XPathErrorFault, UnsupportedSelectorFault, UnsupportedConstraintFault {
		return getMdr().graphQuery(body);
	}
}
