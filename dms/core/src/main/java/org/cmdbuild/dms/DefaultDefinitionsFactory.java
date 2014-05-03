package org.cmdbuild.dms;

import java.util.Collections;

public class DefaultDefinitionsFactory implements DefinitionsFactory {

	@Override
	public DocumentTypeDefinition newDocumentTypeDefinitionWithNoMetadata(final String name) {
		return new DocumentTypeDefinition() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
				return Collections.emptyList();
			}

		};
	}

}
