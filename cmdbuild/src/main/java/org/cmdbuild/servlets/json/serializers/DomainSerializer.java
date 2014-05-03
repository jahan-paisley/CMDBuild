package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.json.JSONException;
import org.json.JSONObject;

public class DomainSerializer extends Serializer {

	private final CMDataView dataView;
	private final PrivilegeContext privilegeContext;

	public DomainSerializer(final CMDataView dataView, final PrivilegeContext privilegeContext) {
		this.dataView = dataView;
		this.privilegeContext = privilegeContext;
	}

	public JSONObject toClient(final CMDomain domain, final boolean activeOnly) throws JSONException {
		return toClient(domain, activeOnly, null);
	}

	public JSONObject toClient(final CMDomain domain, final boolean activeOnly, final String wrapperLabel)
			throws JSONException {
		final JSONObject jsonDomain = new JSONObject();
		jsonDomain.put("idDomain", domain.getId());
		jsonDomain.put("name", domain.getIdentifier().getLocalName());
		jsonDomain.put("origName", domain.getIdentifier().getLocalName());
		jsonDomain.put("description", domain.getDescription());
		jsonDomain.put("descrdir", domain.getDescription1());
		jsonDomain.put("descrinv", domain.getDescription2());

		final CMClass class1 = domain.getClass1();
		if (class1 != null) {
			jsonDomain.put("class1", domain.getClass1().getIdentifier().getLocalName());
			jsonDomain.put("class1id", domain.getClass1().getId());
		}

		final CMClass class2 = domain.getClass2();
		if (class2 != null) {
			jsonDomain.put("class2", domain.getClass2().getIdentifier().getLocalName());
			jsonDomain.put("class2id", domain.getClass2().getId());
		}

		jsonDomain.put("md", domain.isMasterDetail());
		jsonDomain.put("md_label", domain.getMasterDetailDescription());
		jsonDomain.put("classType", getClassType(domain.getIdentifier().getLocalName()));
		jsonDomain.put("active", domain.isActive());
		jsonDomain.put("cardinality", domain.getCardinality());
		// FIXME should not be used in this way
		jsonDomain.put("attributes", AttributeSerializer.withView(dataView)
				.toClient(domain.getAttributes(), activeOnly));

		addAccessPrivileges(jsonDomain, domain);
		// TODO: complete ...
		// addMetadata(jsonDomain, domain);

		if (wrapperLabel != null) {
			final JSONObject out = new JSONObject();
			out.put(wrapperLabel, jsonDomain);
			return out;
		} else {
			return jsonDomain;
		}
	}

	private String getClassType(final String className) {
		// TODO do it better
		final CMClass target = dataView.findClass(className);
		if (dataView.findClass("Activity").isAncestorOf(target)) {
			return "processclass";
		} else {
			return "class";
		}
	}

	private void addAccessPrivileges(final JSONObject jsonObject, final CMDomain domain) throws JSONException {
		final boolean writePrivilege = privilegeContext.hasWriteAccess(domain);
		final boolean createPrivilege = writePrivilege;
		jsonObject.put("priv_write", writePrivilege);
		jsonObject.put("priv_create", createPrivilege);
	}

	public JSONObject toClient(final CMDomain domain, final String className) throws JSONException {
		final JSONObject jsonDomain = toClient(domain, false);
		jsonDomain.put("inherited", !isDomainDefinedForClass(domain, className));
		return jsonDomain;
	}

	/**
	 * @return true if the domain is defined for the class with provided
	 *         classId, false otherwise (it is defined for a superclass)
	 */
	private static boolean isDomainDefinedForClass(final CMDomain domain, final String className) {
		final CMClass class1 = domain.getClass1();
		final CMClass class2 = domain.getClass2();
		return class1.getName().equals(className) || class2.getName().equals(className);
	}
}
