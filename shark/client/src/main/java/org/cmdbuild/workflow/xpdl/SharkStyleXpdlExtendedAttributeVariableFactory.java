package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;

public class SharkStyleXpdlExtendedAttributeVariableFactory implements XpdlExtendedAttributeVariableFactory {

	public enum VariableSuffix {
		VIEW {
			@Override
			public Type toGlobalType() {
				return CMActivityVariableToProcess.Type.READ_ONLY;
			}
		},
		UPDATE {
			@Override
			public Type toGlobalType() {
				return CMActivityVariableToProcess.Type.READ_WRITE;
			}
		},
		UPDATEREQUIRED {
			@Override
			public Type toGlobalType() {
				return CMActivityVariableToProcess.Type.READ_WRITE_REQUIRED;
			}
		};

		public abstract Type toGlobalType();
	}

	public static final String VARIABLE_PREFIX = "VariableToProcess_";

	@Override
	public CMActivityVariableToProcess createVariable(final XpdlExtendedAttribute xa) {
		final String key = xa.getKey();
		final String name = xa.getValue();
		if (key == null || name == null) {
			return null;
		}
		if (isVariableKey(key)) {
			final Type type = extractType(key);
			return new CMActivityVariableToProcess(name, type);
		} else {
			return null;
		}
	}

	private final boolean isVariableKey(final String key) {
		return key.startsWith(VARIABLE_PREFIX);
	}

	private final Type extractType(final String key) {
		final String suffix = key.substring(VARIABLE_PREFIX.length());
		final VariableSuffix xpdlType = VariableSuffix.valueOf(suffix);
		return xpdlType.toGlobalType();
	}

}
