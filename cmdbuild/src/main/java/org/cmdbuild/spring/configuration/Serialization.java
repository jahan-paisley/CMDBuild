package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.servlets.json.serializers.CardSerializer;
import org.cmdbuild.servlets.json.serializers.ClassSerializer;
import org.cmdbuild.servlets.json.serializers.DomainSerializer;
import org.cmdbuild.servlets.json.serializers.RelationAttributeSerializer;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Serialization {

	@Autowired
	private Data data;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Autowired
	private Workflow workflow;

	@Bean
	public CardSerializer cardSerializer() {
		return new CardSerializer(data.systemDataAccessLogicBuilder(), relationAttributeSerializer());
	}

	@Bean
	@Scope(PROTOTYPE)
	public ClassSerializer classSerializer() {
		return new ClassSerializer( //
				data.systemDataView(), //
				workflow.systemWorkflowLogicBuilder(), //
				privilegeManagement.userPrivilegeContext());
	}

	@Bean
	@Scope(PROTOTYPE)
	public DomainSerializer domainSerializer() {
		return new DomainSerializer( //
				data.systemDataView(), //
				privilegeManagement.userPrivilegeContext());
	}

	@Bean
	public RelationAttributeSerializer relationAttributeSerializer() {
		return new RelationAttributeSerializer(data.lookupStore());
	}

}
