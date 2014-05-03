package org.cmdbuild.services.soap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.dmtf.schemas.cmdbf._1.tns.registration.DeregistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidMDRFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidRecordFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationPortType;
import org.dmtf.schemas.cmdbf._1.tns.registration.UnsupportedRecordTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterResponseType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

@WebService(targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/registration", endpointInterface = "org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationPortType")
public class CMDBfRegistrationImpl extends AbstractWebservice implements RegistrationPortType {

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
	@WebResult(name = "registerResponse", targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/serviceData", partName = "body")
	@WebMethod(operationName = "Register")
	public RegisterResponseType register(
			@WebParam(partName = "body", name = "registerRequest", targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/serviceData") final RegisterRequestType body)
			throws UnsupportedRecordTypeFault, InvalidRecordFault, InvalidMDRFault, RegistrationErrorFault {
		return getMdr().register(body);
	}

	@Override
	@WebResult(name = "deregisterResponse", targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/serviceData", partName = "body")
	@WebMethod(operationName = "Deregister")
	public DeregisterResponseType deregister(
			@WebParam(partName = "body", name = "deregisterRequest", targetNamespace = "http://schemas.dmtf.org/cmdbf/1/tns/serviceData") final DeregisterRequestType body)
			throws DeregistrationErrorFault, InvalidMDRFault {
		return getMdr().deregister(body);
	}
}
