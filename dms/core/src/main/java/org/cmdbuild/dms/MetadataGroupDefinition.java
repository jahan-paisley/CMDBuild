package org.cmdbuild.dms;

public interface MetadataGroupDefinition {

	String getName();

	Iterable<MetadataDefinition> getMetadataDefinitions();

}
