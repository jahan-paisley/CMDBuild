package org.cmdbuild.model.bim;

import org.cmdbuild.data.store.Storable;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class StorableLayer implements Storable {

	private String className, description, rootReference;
	private boolean active, root, export, container;
	
	public static StorableLayer NULL_LAYER = new StorableLayer("");
	
	public static boolean isValidLayer(StorableLayer layer){
		return layer != null && !isEmpty(layer.getClassName());
	}
	
	public StorableLayer(String className) {
		this.className = className;
	}

	@Override
	public String getIdentifier() {
		return getClassName();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean bimRoot) {
		this.root = bimRoot;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean enabledBim) {
		this.active = enabledBim;
	}

	public boolean isExport() {
		return export;
	}

	public void setExport(boolean export) {
		this.export = export;
	}

	public boolean isContainer() {
		return container;
	}

	public void setContainer(boolean container) {
		this.container = container;
	}

	public String getRootReference() {
		return rootReference;
	}

	public void setRootReference(String rootReference) {
		this.rootReference = rootReference;
	}
	

}
