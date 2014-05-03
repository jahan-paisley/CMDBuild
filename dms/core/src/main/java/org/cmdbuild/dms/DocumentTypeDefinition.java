package org.cmdbuild.dms;

public interface DocumentTypeDefinition {

	String getName();

	Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions();

}