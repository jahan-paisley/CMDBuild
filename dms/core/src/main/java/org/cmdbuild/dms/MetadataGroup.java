package org.cmdbuild.dms;

public interface MetadataGroup {

	String getName();

	Iterable<Metadata> getMetadata();

}
