package org.cmdbuild.services.soap.types;

import java.util.Calendar;
import java.util.Date;

import org.cmdbuild.dms.StoredDocument;

public class Attachment {
	
	private String filename;
	private String description;
	private String version;
	private String author;
	private String category;
	private Calendar created;
	private Calendar modified;
	
	public Attachment(){ }
	
	public Attachment (StoredDocument bean){
		this.setFilename(bean.getName());
		this.setDescription(bean.getDescription());
		this.setVersion(bean.getVersion());
		this.setAuthor(bean.getAuthor());
		Date dcreated = bean.getCreated();
		Date dmodified = bean.getModified();
		Calendar created = Calendar.getInstance();
		created.setTime(dcreated);
		Calendar modified = Calendar.getInstance();
		modified.setTime(dmodified);
		this.setCreated(created);
		this.setModified(modified);
		this.setCategory(bean.getCategory());
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Calendar getCreated() {
		return created;
	}
	public void setCreated(Calendar created) {
		this.created = created;
	}
	public Calendar getModified() {
		return modified;
	}
	public void setModified(Calendar modified) {
		this.modified = modified;
	}
}
