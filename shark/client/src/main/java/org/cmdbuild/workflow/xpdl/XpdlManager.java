package org.cmdbuild.workflow.xpdl;

import java.io.IOException;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;

/**
 * Process Definition Manager that uses XPDL definitions.
 */
public class XpdlManager extends AbstractProcessDefinitionManager {

	public interface GroupQueryAdapter {

		String[] getAllGroupNames();
	}

	private static final String DEFAULT_SYSTEM_PARTICIPANT = "System";

	private final GroupQueryAdapter groupQueryAdapter;

	public XpdlManager(final GroupQueryAdapter groupQueryAdapter,
			final XpdlProcessDefinitionStore xpdlProcessDefinitionStore, final TemplateResolver templateResolver) {
		super(xpdlProcessDefinitionStore, templateResolver);
		this.groupQueryAdapter = groupQueryAdapter;
	}

	@Override
	public DataSource getTemplate(final CMProcessClass process) throws XpdlException {
		final XpdlDocument doc = new XpdlDocument(getStandardPackageId(process));
		doc.createCustomTypeDeclarations();
		doc.setDefaultScriptingLanguage(ScriptLanguage.JAVA);
		addProcessWithFields(doc, process);
		doc.addSystemParticipant(DEFAULT_SYSTEM_PARTICIPANT);
		addAllGroupsToTemplate(doc);
		// Applications NOT NEEDED?
		return createDataSource(process, doc);
	}

	private DataSource createDataSource(final CMProcessClass process, final XpdlDocument doc) throws XpdlException {
		final byte[] xpdl = XpdlPackageFactory.xpdlByteArray(doc.getPkg());
		final ByteArrayDataSource ds = new ByteArrayDataSource(xpdl, getMimeType());
		ds.setName(String.format("%s.%s", process.getName(), getFileExtension()));
		return ds;
	}

	@Legacy("Should use the new authentication framework, passed as a constructor parameter")
	private void addAllGroupsToTemplate(final XpdlDocument doc) {
		for (final String name : groupQueryAdapter.getAllGroupNames()) {
			doc.addRoleParticipant(name);
		}
	}

	private void addProcessWithFields(final XpdlDocument doc, final CMProcessClass process) {
		final String procDefId = getStandardProcessDefinitionId(process);
		final XpdlProcess proc = doc.createProcess(procDefId);
		addBindedClass(doc, process);
		final DaoToXpdlAttributeTypeConverter typeConverter = new DaoToXpdlAttributeTypeConverter();
		for (final CMAttribute a : process.getAttributes()) {
			final XpdlDocument.StandardAndCustomTypes type = typeConverter.convertType(a.getType());
			if (type != null) {
				proc.addField(a.getName(), type);
			}
		}
	}

	@Legacy("As in 1.x")
	private void addBindedClass(final XpdlDocument doc, final CMProcessClass process) {
		doc.findProcess(getStandardProcessDefinitionId(process)).setBindToClass(process.getName());
	}

	@Override
	public void updateDefinition(final CMProcessClass process, final DataSource pkgDefData) throws CMWorkflowException {
		try {
			final XpdlDocument xpdl = new XpdlDocument(XpdlPackageFactory.readXpdl(pkgDefData.getInputStream()));
			// if (!getStandardPackageId(process).equals(xpdl.getPackageId())) {
			// throw new XpdlException("The package id does not match");
			// }
			final XpdlProcess proc = xpdl.findProcess(getStandardProcessDefinitionId(process));
			if (proc == null) {
				throw new XpdlException("The process id does not match");
			}
			final String bindedClass = proc.getBindToClass();
			if (!process.getName().equals(bindedClass)) {
				throw new XpdlException("The process is not bound to this class");
			}
		} catch (final IOException e) {
			throw new CMWorkflowException(e);
		}
		super.updateDefinition(process, pkgDefData);
	}

	@Override
	protected String getMimeType() {
		return "application/x-xpdl";
	}

	@Override
	protected String getFileExtension() {
		return "xpdl";
	}

	private class DaoToXpdlAttributeTypeConverter implements CMAttributeTypeVisitor {

		private XpdlDocument.StandardAndCustomTypes xpdlType;

		public XpdlDocument.StandardAndCustomTypes convertType(final CMAttributeType<?> type) {
			type.accept(this);
			return xpdlType;
		}

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.BOOLEAN;
		}

		@Override
		public void visit(final CharAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.DATETIME;
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.DATETIME;
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.FLOAT;
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.FLOAT;
		}

		@Override
		public void visit(final EntryTypeAttributeType attributeType) {
			xpdlType = null;
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.REFERENCE;
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.INTEGER;
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.LOOKUP;
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.REFERENCE;
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.STRING;
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			xpdlType = XpdlDocument.StandardAndCustomTypes.DATETIME;
		}

		@Override
		public void visit(StringArrayAttributeType stringArrayAttributeType) {
			xpdlType = null; // TODO verity this
		}
	}

}
