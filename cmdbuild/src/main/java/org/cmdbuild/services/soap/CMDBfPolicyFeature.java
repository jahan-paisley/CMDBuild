package org.cmdbuild.services.soap;

import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.Extensible;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RegistrationServiceMetadata;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Element;

public class CMDBfPolicyFeature extends AbstractFeature implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ManagementDataRepository getMdr() {
		return applicationContext.getBean(ManagementDataRepository.class);
	}
	
	public CMDBfPolicyFeature() {
        super();
    }
	
	@Override
	protected void initializeProvider(InterceptorProvider provider, Bus bus) {
		if(provider instanceof Endpoint) {
			Endpoint endpoint = (Endpoint)provider;
	        EndpointInfo ei = endpoint.getEndpointInfo();
	        Policy policy = getEndpointPolicy(endpoint);
	        addPolicy(ei, policy);
		}
    }

	private Policy getEndpointPolicy(Endpoint endpoint) {
        EndpointInfo ei = endpoint.getEndpointInfo();
        Policy policy = new Policy();
		if (ei.getName().getNamespaceURI().equals("http://schemas.dmtf.org/cmdbf/1/tns/query")) {
			QueryServiceMetadata metadata = getMdr().getQueryServiceMetadata();
			JaxbAssertion<QueryServiceMetadata> assertion = new JaxbAssertion<QueryServiceMetadata>(new QName("http://schemas.dmtf.org/cmdbf/1/tns/serviceMetadata", "queryServiceMetadata"), false);
			assertion.setData(metadata);
			policy.setId("CMDBf-QueryServiceMetadata");
			policy.addAssertion(assertion);
		}
		if (ei.getName().getNamespaceURI().equals("http://schemas.dmtf.org/cmdbf/1/tns/registration")) {
			RegistrationServiceMetadata metadata = getMdr().getRegistrationServiceMetadata();
			JaxbAssertion<RegistrationServiceMetadata> assertion = new JaxbAssertion<RegistrationServiceMetadata>(new QName("http://schemas.dmtf.org/cmdbf/1/tns/serviceMetadata", "registrationServiceMetadata"), false);
			assertion.setData(metadata);
			policy.setId("CMDBf-RegistrationServiceMetadata");
			policy.addAssertion(assertion);
		}		
        return policy;
	}

    private void addPolicy(Extensible ext, Policy policy) {
        try {
            W3CDOMStreamWriter writer = new W3CDOMStreamWriter();
            policy.serialize(writer);
            Element policyEl = writer.getDocument().getDocumentElement();
            policyEl.removeAttribute("xmlns:xmlns");

            UnknownExtensibilityElement uee = new UnknownExtensibilityElement();
            uee.setElementType(new QName(Constants.URI_POLICY_NS, Constants.ELEM_POLICY));
            uee.setElement(policyEl);

            ext.addExtensor(uee);
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Could not serialize policy", ex);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not serialize policy", e);
        }        
    }
}
