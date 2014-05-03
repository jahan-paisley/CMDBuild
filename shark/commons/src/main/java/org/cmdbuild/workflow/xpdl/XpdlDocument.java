package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.cmdbuild.workflow.Constants;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.jxpdl.XMLInterface;
import org.enhydra.jxpdl.XMLInterfaceImpl;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.DataField;
import org.enhydra.jxpdl.elements.DataTypes;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.jxpdl.elements.Participant;
import org.enhydra.jxpdl.elements.TypeDeclaration;
import org.enhydra.jxpdl.elements.TypeDeclarations;
import org.enhydra.jxpdl.elements.WorkflowProcess;
import org.enhydra.jxpdl.elements.WorkflowProcesses;
import org.enhydra.shark.api.common.SharkConstants;

/**
 * It makes easier the handling of the XPDL DOM
 */
@NotThreadSafe
public class XpdlDocument {

	public enum ScriptLanguage {
		JAVA(SharkConstants.GRAMMAR_JAVA), //
		JAVASCRIPT(SharkConstants.GRAMMAR_JAVA_SCRIPT), //
		PYTHON(SharkConstants.GRAMMAR_PYTHON_SCRIPT), // 
		GROOVY("text/groovy"), //
		;

		private final String mimeType;

		private ScriptLanguage(final String mimeType) {
			this.mimeType = mimeType;
		}

		public String getMimeType() {
			return mimeType;
		}

		public static ScriptLanguage of(final String mimeType) {
			for (ScriptLanguage language : values()) {
				if (language.mimeType.equals(mimeType)) {
					return language;
				}
			}
			throw new IllegalArgumentException("invalid mime-type");
		}

	}

	public enum StandardAndCustomTypes {
		BOOLEAN {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeBOOLEAN();
			}
		},
		DATETIME {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeDATETIME();
			}
		},
		FLOAT {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeFLOAT();
			}
		},
		INTEGER {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeINTEGER();
			}
		},
		STRING {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.getBasicType().setTypeSTRING();
			}
		},
		/*
		 * For backward compatibility
		 */
		REFERENCE(Constants.XPDL_REFERENCE_DECLARED_TYPE, ReferenceType.class.getName()) {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.setDeclaredType();
				dataTypes.getDeclaredType().setId(getDeclaredTypeId());
			}
		},
		LOOKUP(Constants.XPDL_LOOKUP_DECLARED_TYPE, LookupType.class.getName()) {
			@Override
			protected void selectDataType(DataTypes dataTypes) {
				dataTypes.setDeclaredType();
				dataTypes.getDeclaredType().setId(getDeclaredTypeId());
			}
		};

		private final String declaredTypeId;
		private final String declaredTypeLocation;

		private StandardAndCustomTypes() {
			this.declaredTypeId = null;
			this.declaredTypeLocation = null;
		}

		private StandardAndCustomTypes(final String declaredTypeId, final String declaredTypeLocation) {
			this.declaredTypeId = declaredTypeId;
			this.declaredTypeLocation = declaredTypeLocation;
		}

		public boolean isCustom() {
			return (declaredTypeId != null);
		}

		public String getDeclaredTypeId() {
			return declaredTypeId;
		}

		public String getDeclaredTypeLocation() {
			return declaredTypeLocation;
		}

		public void setTypeToField(final DataField df) {
			selectDataType(df.getDataType().getDataTypes());
		}

		abstract protected void selectDataType(DataTypes dataTypes);
	}

	/*
	 * Wish it was not a magic constant in txm!
	 * 
	 * http://en.wikipedia.org/wiki/Magic_number_(programming)#
	 * Unnamed_numerical_constants
	 */
	private static final String DEFAULT_XPDL_VERSION = "2.1";

	public static final String ARRAY_DECLARED_TYPE_NAME_SUFFIX = Constants.XPDL_ARRAY_DECLARED_TYPE_SUFFIX;
	public static final String ARRAY_DECLARED_TYPE_LOCATION_SUFFIX = "<>";

	private final Package pkg;
	private final XMLInterface xmlInterface;

	public XpdlDocument(final String pkgId) {
		this(new Package());
		pkg.setId(pkgId);
		pkg.getPackageHeader().setXPDLVersion(DEFAULT_XPDL_VERSION);
	}

	public XpdlDocument(final Package pkg) {
		xmlInterface = new XMLInterfaceImpl();
		this.pkg = pkg;
	}

	public Package getPkg() {
		return pkg;
	}

	public String getPackageId() {
		return pkg.getId();
	}

	public XpdlProcess createProcess(final String procDefId) {
		turnReadWrite();
		WorkflowProcess wp = (WorkflowProcess) pkg.getWorkflowProcesses().generateNewElement();
		wp.setId(procDefId);
		pkg.getWorkflowProcesses().add(wp);
		return new XpdlProcess(this, wp);
	}

	public XpdlProcess findProcess(final String procDefId) {
		final WorkflowProcess wp = pkg.getWorkflowProcess(procDefId);
		if (wp != null) {
			return new XpdlProcess(this, wp);
		} else {
			return null;
		}
	}

	public List<XpdlProcess> findAllProcesses() {
		final WorkflowProcesses wps = pkg.getWorkflowProcesses();
		final List<XpdlProcess> out = new ArrayList<XpdlProcess>(wps.size());
		for (int i = 0; i < wps.size(); ++i) {
			final WorkflowProcess wp = (WorkflowProcess) wps.get(i);
			out.add(new XpdlProcess(this, wp));
		}
		return out;
	}

	public void addPackageField(final String dfId, final StandardAndCustomTypes type) {
		turnReadWrite();
		DataField df = createDataField(dfId, type);
		pkg.getDataFields().add(df);
	}

	DataField createDataField(final String dfId, final StandardAndCustomTypes type) {
		DataField df = (DataField) pkg.getDataFields().generateNewElement();
		df.setId(dfId);
		type.setTypeToField(df);
		return df;
	}

	public void setDefaultScriptingLanguage(final ScriptLanguage lang) {
		pkg.getScript().setType(lang.mimeType);
	}

	public void addRoleParticipant(final String participantId) {
		turnReadWrite();
		Participant p = (Participant) pkg.getParticipants().generateNewElement();
		p.setId(participantId);
		// Default but better safe than sorry
		p.getParticipantType().setTypeROLE();
		pkg.getParticipants().add(p);
	}

	public boolean hasRoleParticipant(final String participantId) {
		final Participant p = pkg.getParticipants().getParticipant(participantId);
		return (p != null) && (XPDLConstants.PARTICIPANT_TYPE_ROLE.equals(p.getParticipantType().getType()));
	}

	public void addSystemParticipant(final String participantId) {
		turnReadWrite();
		Participant p = (Participant) pkg.getParticipants().generateNewElement();
		p.setId(participantId);
		p.getParticipantType().setTypeSYSTEM();
		pkg.getParticipants().add(p);
	}

	/*
	 * For backward compatibility
	 */

	public void createCustomTypeDeclarations() {
		TypeDeclarations types = pkg.getTypeDeclarations();
		for (StandardAndCustomTypes t : StandardAndCustomTypes.values()) {
			if (t.isCustom()) {
				addExternalReferenceType(types, t);
				addExternalReferenceArrayType(types, t);
			}
		}
	}

	private void addExternalReferenceType(TypeDeclarations types, StandardAndCustomTypes t) {
		addExternalReferenceType(types, t.getDeclaredTypeId(), t.getDeclaredTypeLocation());
	}

	private void addExternalReferenceArrayType(TypeDeclarations types, StandardAndCustomTypes t) {
		addExternalReferenceType(types, t.getDeclaredTypeId() + ARRAY_DECLARED_TYPE_NAME_SUFFIX, t
				.getDeclaredTypeLocation()
				+ ARRAY_DECLARED_TYPE_LOCATION_SUFFIX);
	}

	private void addExternalReferenceType(final TypeDeclarations types, final String id, final String location) {
		turnReadWrite();
		TypeDeclaration type = (TypeDeclaration) types.generateNewElement();
		type.setId(id);
		type.getDataTypes().getExternalReference().setLocation(location);
		type.getDataTypes().setExternalReference();
		types.add(type);
	}

	/**
	 * Aberration because the library does not allow graph traversal when in
	 * read/write mode. This function should be called before querying the graph
	 * unless elements are accessed by id.
	 * 
	 * We are more interested in development speed than running speed, so we put
	 * the whole package in read only.
	 */
	void turnReadOnly() {
		if (!pkg.isReadOnly()) {
			pkg.setReadOnly(true);
			pkg.initCaches(xmlInterface);
		}
	}

	/**
	 * Aberration because the library rejects changes to the tree when in read
	 * only mode. This function should be called before every "add" operation.
	 * 
	 * We are more interested in development speed than running speed, so we put
	 * the whole package in read write.
	 */
	void turnReadWrite() {
		if (pkg.isReadOnly()) {
			pkg.setReadOnly(false);
		}
	}
}
