package org.cmdbuild.dms;

import java.util.Date;

public class StoredDocument {

	private String name;
	private String uuid;
	private String description;
	private String version;
	private String author;
	private Date created;
	private Date modified;
	private String category;
	private Iterable<MetadataGroup> metadataGroups;

	private String path;

	public String getAuthor() {
		return author;
	}

	public void setAuthor(final String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(final Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(final Date modified) {
		this.modified = modified;
	}

	public Iterable<MetadataGroup> getMetadataGroups() {
		return metadataGroups;
	}

	public void setMetadataGroups(final Iterable<MetadataGroup> metadataGroups) {
		this.metadataGroups = metadataGroups;
	}

	/*
	 * here are the things alfresco returns when searching for something (at
	 * least the things i think are useful)
	 * 
	 * {http://www.alfresco.org/model/content/1.0}name :
	 * cmdb.20070307.backup.sql
	 * 
	 * 
	 * {http://www.alfresco.org/model/system/1.0}node-dbid : 2680
	 * 
	 * {http://www.alfresco.org/model/content/1.0}modified :
	 * 2007-09-25T15:45:58.346+02:00
	 * 
	 * {http://www.alfresco.org/model/content/1.0}initialVersion : true
	 * 
	 * {http://www.alfresco.org/model/content/1.0}description :
	 * cmdb.20070307.backup.sql
	 * 
	 * {http://www.alfresco.org/model/system/1.0}node-uuid :
	 * a02cf3a3-6b6d-11dc-8bb4-c5afb2bb3313
	 * 
	 * {http://www.alfresco.org/model/content/1.0}autoVersion : true
	 * 
	 * {http://www.alfresco.org/model/system/1.0}store-protocol : workspace
	 * 
	 * {http://www.alfresco.org/model/content/1.0}modifier : admin
	 * 
	 * {http://www.alfresco.org/model/content/1.0}title :
	 * cmdb.20070307.backup.sql
	 * 
	 * {http://www.alfresco.org/model/content/1.0}content :
	 * contentUrl=store://2007
	 * /9/25/15/45/a03cd226-6b6d-11dc-8bb4-c5afb2bb3313.bin
	 * |mimetype=text/plain|size=15364335|encoding=UTF-8|locale=en_AU_
	 * 
	 * {http://www.alfresco.org/model/system/1.0}store-identifier : SpacesStore
	 * 
	 * {http://www.alfresco.org/model/content/1.0}created :
	 * 2007-09-25T15:45:49.800+02:00
	 * 
	 * {http://www.alfresco.org/model/content/1.0}versionLabel : 1.0
	 * 
	 * {http://www.alfresco.org/model/content/1.0}creator : admin
	 * 
	 * {http://www.alfresco.org/model/content/1.0}path :
	 * /{http://www.alfresco.org
	 * /model/application/1.0}company_home/{http://www.alfresco
	 * .org/model/application
	 * /1.0}user_homes/{http://www.alfresco.org/model/content
	 * /1.0}CMDBuild/{http:
	 * //www.alfresco.org/model/content/1.0}test/{http://www.
	 * alfresco.org/model/content
	 * /1.0}asset/{http://www.alfresco.org/model/content
	 * /1.0}aClassName/{http://www
	 * .alfresco.org/model/content/1.0}cmdb.20070307.backup.sql
	 */
}
